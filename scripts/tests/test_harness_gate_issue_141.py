# Coordinator 실행 안전 게이트의 경로·재시도·heartbeat 판정을 검증하는 테스트
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

from scripts import harness_gate
from scripts import team_orchestration as team


class WorktreePathGateTest(unittest.TestCase):
	def test_ascii_worktree_is_allowed_for_java_task(self):
		self.assertEqual(
			"ALLOW",
			harness_gate.worktree_path_action(
				"C:/coffee-order-system-issue-141",
				java_or_gradle_required=True,
			),
		)

	def test_non_ascii_worktree_is_blocked_before_java_task(self):
		self.assertEqual(
			"BLOCKED: NON_ASCII_WORKTREE_PATH",
			harness_gate.worktree_path_action(
				"C:/Users/user/Documents/커피 주문 시스템/issue-141",
				java_or_gradle_required=True,
			),
		)

	def test_non_ascii_worktree_is_irrelevant_for_python_only_task(self):
		self.assertEqual(
			"ALLOW",
			harness_gate.worktree_path_action(
				"C:/Users/user/Documents/커피 주문 시스템/issue-141",
				java_or_gradle_required=False,
			),
		)


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
		self.assertEqual("RETRY_ONCE", state.retry_decision())
		self.assertEqual(1, state.retries)
		self.assertEqual("BLOCKED: RETRY_LIMIT", state.retry_decision())
		self.assertEqual("START_APPROVED_NEW_RUN", state.retry_decision(user_approved_new_run=True))
		self.assertEqual(1, state.retries)


class AssignmentLifecycleTest(unittest.TestCase):
	def assignment(self):
		return team.AgentAssignment(
			agent_id="dev-menu",
			role="Dev",
			worktree_path="C:/coffee-order-system-issue-141",
			owned_paths=("src/main/**",),
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
		self.assertEqual(
			"TIMEOUT",
			state.lifecycle_status("dev-menu", now=200.0, heartbeat_timeout=30.0),
		)
		self.assertEqual("TIMEOUT", state.assignments["dev-menu"].status)

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
		assignment = team.AgentAssignment(
			agent_id="dev-korean-path",
			role="Dev",
			worktree_path="C:/Users/user/Documents/커피 주문 시스템/issue-141",
			owned_paths=("src/main/**",),
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
					"--java-or-gradle-required",
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


if __name__ == "__main__":
	unittest.main()
