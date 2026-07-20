# Coordinator 실행 안전 게이트의 경로·재시도·heartbeat 판정을 검증하는 테스트
import tempfile
import unittest
from pathlib import Path
from contextlib import redirect_stdout
from io import StringIO
from unittest.mock import patch

from scripts import harness_gate
from scripts import team_orchestration as team


class WorktreePathGateTest(unittest.TestCase):
	def test_ascii_worktree_is_allowed_for_java_task(self):
		self.assertEqual(
			"ALLOW",
			harness_gate.worktree_path_action(
				str(Path.cwd()),
				java_or_gradle_required=True,
			),
		)

	def test_non_ascii_worktree_is_blocked_before_java_task(self):
		with tempfile.TemporaryDirectory() as temp_dir:
			worktree = Path(temp_dir) / "커피"
			worktree.mkdir()
			self.assertEqual(
				"BLOCKED: NON_ASCII_WORKTREE_PATH",
				harness_gate.worktree_path_action(str(worktree), java_or_gradle_required=True),
			)

	def test_non_ascii_worktree_is_irrelevant_for_python_only_task(self):
		with tempfile.TemporaryDirectory() as temp_dir:
			worktree = Path(temp_dir) / "커피"
			worktree.mkdir()
			self.assertEqual("ALLOW", harness_gate.worktree_path_action(str(worktree), java_or_gradle_required=False))


class RetryCircuitBreakerTest(unittest.TestCase):
	def test_first_failure_allows_one_retry(self):
		self.assertEqual("RETRY_ONCE", harness_gate.retry_action(retry_count=0))

	def test_second_failure_blocks_without_new_run_approval(self):
		self.assertEqual(
			"BLOCKED: RETRY_LIMIT",
			harness_gate.retry_action(retry_count=1),
		)

	def test_explicit_new_run_approval_does_not_reset_retry_count(self):
		self.assertEqual(
			"START_APPROVED_NEW_RUN",
			harness_gate.retry_action(retry_count=1, user_approved_new_run=True),
		)

	def test_negative_retry_count_is_rejected(self):
		with self.assertRaises(ValueError):
			harness_gate.retry_action(retry_count=-1)

	def test_team_state_enforces_retry_budget_without_resetting_it(self):
		state = team.TeamState.new()
		self.assertEqual("RETRY_ONCE", state.retry_decision(issue=141, failure_key="first-failure"))
		self.assertEqual(1, state.retries)
		self.assertEqual("BLOCKED: RETRY_LIMIT", state.retry_decision(issue=141, failure_key="first-failure"))
		self.assertEqual("RETRY_ONCE", state.retry_decision(issue=142, failure_key="first-failure"))
		self.assertEqual("RETRY_ONCE", state.retry_decision(issue=141, failure_key="other-failure"))
		self.assertEqual(3, state.retries)


class AssignmentLifecycleTest(unittest.TestCase):
	def assignment(self):
		return team.AgentAssignment(
			agent_id="dev-menu",
			role="Dev",
			worktree_path=str(Path.cwd()),
			owned_paths=("src/main/**",),
			impact="harness lifecycle",
			java_or_gradle_required=False,
			started_at=100.0,
			last_heartbeat_at=100.0,
			deadline_at=200.0,
		)

	def test_register_initializes_running_assignment_lifecycle(self):
		state = team.TeamState.new()
		self.assertEqual("REGISTERED", state.register_agent(self.assignment()).status)
		assignment = state.assignments["dev-menu"]
		self.assertEqual("RUNNING", assignment.status)
		self.assertEqual(100.0, assignment.started_at)
		self.assertEqual(100.0, assignment.last_heartbeat_at)

	def test_heartbeat_updates_phase_and_timestamp(self):
		state = team.TeamState.new()
		state.register_agent(self.assignment())
		self.assertTrue(state.heartbeat("dev-menu", now=110.0, phase="VERIFY"))
		assignment = state.assignments["dev-menu"]
		self.assertEqual("VERIFY", assignment.phase)
		self.assertEqual(110.0, assignment.last_heartbeat_at)

	def test_lifecycle_status_distinguishes_running_stalled_and_timeout(self):
		state = team.TeamState.new()
		state.register_agent(self.assignment())
		self.assertEqual(
			"RUNNING",
			state.lifecycle_status("dev-menu", now=120.0, heartbeat_timeout=30.0),
		)
		self.assertEqual(
			"STALLED",
			state.lifecycle_status("dev-menu", now=131.0, heartbeat_timeout=30.0),
		)
		self.assertEqual("STALLED", state.assignments["dev-menu"].status)
		self.assertEqual("STALLED", state.lifecycle_status("dev-menu", now=200.0, heartbeat_timeout=30.0))
		self.assertEqual(1, state.stalls)

	def test_live_assignment_returns_wait_action_without_polling(self):
		state = team.TeamState.new()
		state.register_agent(self.assignment())
		self.assertEqual(
			"WAIT",
			state.lifecycle_action("dev-menu", now=120.0, heartbeat_timeout=30.0),
		)

	def test_lifecycle_fields_survive_state_round_trip(self):
		state = team.TeamState.new()
		state.register_agent(self.assignment())
		state.heartbeat("dev-menu", now=110.0, phase="VERIFY")
		with tempfile.TemporaryDirectory() as temp_dir:
			team.save_state(Path(temp_dir), state)
			loaded = team.load_state(Path(temp_dir))
		assignment = loaded.assignments["dev-menu"]
		self.assertEqual("VERIFY", assignment.phase)
		self.assertEqual("RUNNING", assignment.status)
		self.assertEqual(100.0, assignment.started_at)
		self.assertEqual(110.0, assignment.last_heartbeat_at)
		self.assertEqual(200.0, assignment.deadline_at)

	def test_heartbeat_cannot_resurrect_terminal_assignment(self):
		state = team.TeamState.new()
		state.register_agent(self.assignment())
		state.mark_terminal("dev-menu", status="TIMEOUT")
		self.assertFalse(state.heartbeat("dev-menu", now=210.0, phase="VERIFY"))

	def test_java_assignment_with_non_ascii_worktree_is_blocked_at_registration(self):
		state = team.TeamState.new()
		with tempfile.TemporaryDirectory() as temp_dir:
			worktree = Path(temp_dir) / "커피" / "issue-141"
			worktree.mkdir(parents=True)
			assignment = team.AgentAssignment(
				agent_id="dev-korean-path", role="Dev", worktree_path=str(worktree),
				owned_paths=("src/main/**",), impact="Java test", deadline_at=200.0,
				java_or_gradle_required=True,
			)
			result = state.register_agent(assignment)
			self.assertEqual("BLOCKED", result.status)
			self.assertIn("NON_ASCII_WORKTREE_PATH", result.detail)
			self.assertEqual(0, state.active_count())

	def test_cli_registration_exposes_java_worktree_gate(self):
		state = team.TeamState.new()
		with patch.object(team, "load_state", return_value=state), patch.object(team, "save_state"), patch(
			"builtins.print"
		):
			result = team.main(
				[
					"register",
					"--agent-id",
					"dev-korean-path",
					"--worktree",
					"C:/Users/user/Documents/커피 주문 시스템/issue-141",
					"--owned-path",
					"src/main/**",
					"--impact",
					"Java test",
					"--deadline-seconds",
					"60",
					"--java-required",
				]
			)
		self.assertEqual(1, result)
		self.assertEqual(0, state.active_count())


class DocumentationContractTest(unittest.TestCase):
	def test_policy_and_agent_rules_describe_enforced_runtime_guards(self):
		repository_root = Path(__file__).resolve().parents[2]
		policy = (repository_root / "docs/ai/orchestration-policy.md").read_text(encoding="utf-8")
		rules = (repository_root / "docs/ai/agent-rules.md").read_text(encoding="utf-8")
		skill = (
			repository_root / ".codex/skills/coffee-order-issue-loop/SKILL.md"
		).read_text(encoding="utf-8")
		for content in (policy, rules, skill):
			self.assertIn("NON_ASCII_WORKTREE_PATH", content)
			self.assertIn("RETRY_LIMIT", content)
			self.assertIn("heartbeat", content.lower())
			self.assertIn("deadline", content.lower())

	def test_policy_keeps_runtime_monitoring_out_of_scope(self):
		repository_root = Path(__file__).resolve().parents[2]
		policy = (repository_root / "docs/ai/orchestration-policy.md").read_text(encoding="utf-8")
		self.assertIn("shell polling", policy)
		self.assertIn("Agent가 멈추면", policy)

	def test_pr_metadata_requires_unbulleted_execution_mode_declarations(self):
		valid = "Execution mode: STRICT\nExecution mode reason: focused harness regression"
		self.assertEqual([], harness_gate.validate_execution_mode_fields(valid))
		invalid = "- Execution mode: STRICT\n- Execution mode reason: focused harness regression"
		self.assertTrue(harness_gate.validate_execution_mode_fields(invalid))

	def test_github_korean_metadata_rule_uses_utf8_body_file(self):
		repository_root = Path(__file__).resolve().parents[2]
		contents = (
			(repository_root / "docs/ai/orchestration-policy.md").read_text(encoding="utf-8"),
			(repository_root / "docs/ai/agent-rules.md").read_text(encoding="utf-8"),
			(repository_root / ".codex/skills/coffee-order-issue-loop/SKILL.md").read_text(encoding="utf-8"),
		)
		for content in contents:
			self.assertIn("UTF-8", content)
			self.assertIn("body-file", content)


class ImpactClassificationTest(unittest.TestCase):
	def test_issue_141_python_fixture_does_not_require_java_ci(self):
		impact = harness_gate.classify_change_impact(
			[harness_gate.ChangeRecord("A", "scripts/tests/test_harness_gate_issue_141.py")],
			141,
		)
		self.assertEqual("STRICT", impact.execution_mode_floor)
		self.assertFalse(impact.requires_java_ci)
		self.assertTrue(impact.invalidates_review_qa)
		self.assertFalse(impact.invalidates_runtime_evidence)


class CoordinatorStateCliFlowTest(unittest.TestCase):
	def run_cli(self, repository_root, *arguments):
		output = StringIO()
		with redirect_stdout(output):
			code = team.main(["--repository-root", str(repository_root), *arguments])
		return code, output.getvalue()

	def test_cli_persists_fail_closed_lifecycle_snapshot_and_scoped_retry_flow(self):
		with tempfile.TemporaryDirectory() as temp_dir:
			repository_root = Path(temp_dir)
			worktree = repository_root / "worktree"
			worktree.mkdir()
			register = (
				"register", "--agent-id", "dev-141", "--worktree", str(worktree),
				"--owned-path", "scripts/**", "--impact", "harness policy",
				"--deadline-seconds", "60", "--no-java-required",
			)
			self.assertEqual(0, self.run_cli(repository_root, *register)[0])
			self.assertEqual(
				0,
				self.run_cli(repository_root, "heartbeat", "--agent-id", "dev-141", "--phase", "VERIFY")[0],
			)
			lifecycle_code, lifecycle = self.run_cli(
				repository_root, "lifecycle", "--agent-id", "dev-141",
				"--heartbeat-timeout-seconds", "0",
			)
			self.assertEqual(0, lifecycle_code)
			self.assertIn("STALLED", lifecycle)
			self.assertEqual(0, self.run_cli(repository_root, "snapshot", "--agent-id", "dev-141")[0])
			second_snapshot, snapshot_output = self.run_cli(
				repository_root, "snapshot", "--agent-id", "dev-141"
			)
			self.assertEqual(1, second_snapshot)
			self.assertIn("SNAPSHOT_LIMIT", snapshot_output)
			first_retry, _ = self.run_cli(
				repository_root, "retry", "--issue", "141", "--failure-key", "heartbeat-stalled"
			)
			second_retry, retry_output = self.run_cli(
				repository_root, "retry", "--issue", "141", "--failure-key", "heartbeat-stalled"
			)
			self.assertEqual(0, first_retry)
			self.assertEqual(1, second_retry)
			self.assertIn("RETRY_LIMIT", retry_output)
			self.assertEqual(
				0,
				self.run_cli(repository_root, "release", "--agent-id", "dev-141")[0],
			)
			state = team.load_state(repository_root)
			self.assertEqual(0, state.active_count())
			self.assertEqual(1, state.retry_ledger["141:heartbeat-stalled"])

	def test_registration_requires_existing_worktree_impact_deadline_and_java_declaration(self):
		with tempfile.TemporaryDirectory() as temp_dir:
			repository_root = Path(temp_dir)
			worktree = repository_root / "worktree"
			worktree.mkdir()
			base = ("register", "--agent-id", "dev-141", "--worktree", str(worktree), "--owned-path", "scripts/**")
			for missing in (
				(),
				("--impact", "harness", "--no-java-required"),
				("--impact", "harness", "--deadline-seconds", "60"),
			):
				with self.subTest(missing=missing):
					with self.assertRaises(SystemExit):
						team.main(["--repository-root", str(repository_root), *base, *missing])
			result = team.TeamState.new().register_agent(
				team.AgentAssignment(
					agent_id="direct", role="Dev", worktree_path=str(worktree),
					owned_paths=("scripts/**",), impact="harness", deadline_at=100.0,
				)
			)
			self.assertEqual("BLOCKED", result.status)
			self.assertIn("JAVA_REQUIREMENT_UNDECLARED", result.detail)

	def test_resolved_non_ascii_parent_and_missing_worktree_are_blocked(self):
		with tempfile.TemporaryDirectory() as temp_dir:
			repository_root = Path(temp_dir)
			missing = repository_root / "missing"
			self.assertEqual(
				"BLOCKED: WORKTREE_NOT_FOUND",
				team._worktree_path_action(str(missing), java_or_gradle_required=False),
			)
			non_ascii = repository_root / "커피" / "worktree"
			non_ascii.mkdir(parents=True)
			self.assertEqual(
				"BLOCKED: NON_ASCII_WORKTREE_PATH",
				team._worktree_path_action(str(non_ascii), java_or_gradle_required=True),
			)

	def test_stalled_and_timeout_transitions_are_idempotent_and_free_slots(self):
		with tempfile.TemporaryDirectory() as temp_dir:
			worktree = Path(temp_dir) / "worktree"
			worktree.mkdir()
			state = team.TeamState.new()
			stalled = team.AgentAssignment(
				agent_id="stalled", role="Dev", worktree_path=str(worktree), owned_paths=("scripts/**",),
				impact="harness", java_or_gradle_required=False, started_at=100.0,
				last_heartbeat_at=100.0, deadline_at=200.0,
			)
			self.assertEqual("REGISTERED", state.register_agent(stalled).status)
			self.assertEqual("STALLED", state.lifecycle_status("stalled", now=131.0, heartbeat_timeout=30.0))
			self.assertEqual("STALLED", state.lifecycle_status("stalled", now=300.0, heartbeat_timeout=30.0))
			self.assertFalse(state.heartbeat("stalled", now=132.0))
			self.assertEqual(0, state.active_count())
			self.assertEqual(0, state.writer_count())
			self.assertEqual(1, state.stalls)
			timed_out = team.AgentAssignment(
				agent_id="timed-out", role="Dev", worktree_path=str(worktree), owned_paths=("docs/**",),
				impact="docs", java_or_gradle_required=False, started_at=100.0,
				last_heartbeat_at=100.0, deadline_at=120.0,
			)
			self.assertEqual("REGISTERED", state.register_agent(timed_out).status)
			self.assertEqual("TIMEOUT", state.lifecycle_status("timed-out", now=120.0, heartbeat_timeout=30.0))
			self.assertEqual("TIMEOUT", state.lifecycle_status("timed-out", now=130.0, heartbeat_timeout=30.0))
			self.assertEqual(1, state.stalls)

	def test_reset_requires_nonempty_approval_and_legacy_state_remains_readable(self):
		with tempfile.TemporaryDirectory() as temp_dir:
			repository_root = Path(temp_dir)
			state_path = team.state_file_path(repository_root)
			state_path.parent.mkdir()
			state_path.write_text(
				'{"config":{"max_active_agents":3,"max_writer_agents":2},"created_at":1.0,"assignments":{},"messages":[],"retries":0,"stalls":0,"out_of_scope_changes":0,"review_qa_defects":0,"human_interventions":0}',
				encoding="utf-8",
			)
			self.assertEqual(0, team.load_state(repository_root).active_count())
			code, output = self.run_cli(repository_root, "reset", "--approval-ref", "")
			self.assertEqual(1, code)
			self.assertIn("APPROVAL_REFERENCE_REQUIRED", output)
			self.assertTrue(state_path.exists())


if __name__ == "__main__":
	unittest.main()
