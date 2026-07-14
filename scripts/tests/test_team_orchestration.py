# Issue #91 병렬 오케스트레이션 실험 도구의 슬롯 한도, owned-path 충돌, 메시지 프로토콜, 합성 smoke를 검증하는 테스트
import inspect
import tempfile
import unittest
from pathlib import Path

from scripts import team_orchestration as team


def menu_assignment(agent_id="dev-menu", is_writer=True):
	return team.AgentAssignment(
		agent_id=agent_id,
		role="Dev",
		worktree_path="../wt-menu",
		owned_paths=("src/main/java/com/example/coffeeordersystem/menu/**",),
		is_writer=is_writer,
	)


def point_assignment(agent_id="dev-point", is_writer=True):
	return team.AgentAssignment(
		agent_id=agent_id,
		role="Dev",
		worktree_path="../wt-point",
		owned_paths=("src/main/java/com/example/coffeeordersystem/point/**",),
		is_writer=is_writer,
	)


class ConfigLimitTest(unittest.TestCase):
	def test_defaults_match_issue_91(self):
		config = team.TeamOrchestrationConfig()
		self.assertEqual(3, config.max_active_agents)
		self.assertEqual(2, config.max_writer_agents)

	def test_three_dev_exception_is_rejected(self):
		with self.assertRaises(team.TeamOrchestrationError):
			team.TeamOrchestrationConfig(max_writer_agents=3)

	def test_writer_limit_cannot_exceed_active_limit(self):
		with self.assertRaises(team.TeamOrchestrationError):
			team.TeamOrchestrationConfig(max_active_agents=1, max_writer_agents=2)

	def test_zero_or_negative_limits_are_rejected(self):
		for kwargs in ({"max_active_agents": 0}, {"max_writer_agents": 0}):
			with self.subTest(kwargs=kwargs):
				with self.assertRaises(team.TeamOrchestrationError):
					team.TeamOrchestrationConfig(**kwargs)

	def test_resolved_shrinks_to_runtime_cpu_budget(self):
		config = team.TeamOrchestrationConfig(max_active_agents=3, max_writer_agents=2)
		resolved = config.resolved()
		self.assertLessEqual(resolved.max_active_agents, config.max_active_agents)
		self.assertLessEqual(resolved.max_writer_agents, config.max_writer_agents)
		self.assertGreaterEqual(resolved.max_active_agents, 1)
		self.assertGreaterEqual(resolved.max_writer_agents, 1)

	def test_resolved_never_exceeds_writer_ceiling_even_with_many_cpus(self):
		config = team.TeamOrchestrationConfig(max_active_agents=2, max_writer_agents=2)
		resolved = config.resolved()
		self.assertLessEqual(resolved.max_writer_agents, team.MAX_WRITER_AGENTS_CEILING)


class OwnedPathOverlapTest(unittest.TestCase):
	def test_identical_paths_overlap(self):
		self.assertIsNotNone(team.owned_paths_overlap(("src/a/**",), ("src/a/**",)))

	def test_nested_paths_overlap(self):
		self.assertIsNotNone(
			team.owned_paths_overlap(
				("src/main/java/com/example/coffeeordersystem/menu/**",),
				("src/main/java/com/example/coffeeordersystem/menu/service/**",),
			)
		)

	def test_sibling_paths_do_not_overlap(self):
		self.assertIsNone(
			team.owned_paths_overlap(
				("src/main/java/com/example/coffeeordersystem/menu/**",),
				("src/main/java/com/example/coffeeordersystem/point/**",),
			)
		)

	def test_shared_prefix_but_different_directory_does_not_overlap(self):
		# "menu" and "menuitem" share a string prefix but are different directories.
		self.assertIsNone(
			team.owned_paths_overlap(("src/main/menu/**",), ("src/main/menuitem/**",))
		)


class RegistrationTest(unittest.TestCase):
	def test_two_independent_writers_both_register(self):
		state = team.TeamState.new()
		result_a = state.register_agent(menu_assignment())
		result_b = state.register_agent(point_assignment())
		self.assertEqual("REGISTERED", result_a.status)
		self.assertEqual("REGISTERED", result_b.status)
		self.assertEqual(2, state.active_count())
		self.assertEqual(2, state.writer_count())

	def test_third_writer_is_blocked_by_default_ceiling(self):
		state = team.TeamState.new()
		state.register_agent(menu_assignment())
		state.register_agent(point_assignment())
		third = team.AgentAssignment(
			agent_id="dev-order",
			role="Dev",
			worktree_path="../wt-order",
			owned_paths=("src/main/java/com/example/coffeeordersystem/order/**",),
		)
		result = state.register_agent(third)
		self.assertEqual("BLOCKED", result.status)
		self.assertEqual(2, state.active_count())

	def test_overlapping_owned_path_is_scope_conflict_not_blocked(self):
		state = team.TeamState.new()
		state.register_agent(menu_assignment())
		conflicting = team.AgentAssignment(
			agent_id="dev-menu-2",
			role="Dev",
			worktree_path="../wt-menu-2",
			owned_paths=("src/main/java/com/example/coffeeordersystem/menu/service/**",),
		)
		result = state.register_agent(conflicting)
		self.assertEqual("SCOPE_CONFLICT", result.status)
		self.assertEqual("dev-menu", result.conflicting_agent_id)
		self.assertEqual(1, state.active_count())

	def test_duplicate_agent_id_is_blocked(self):
		state = team.TeamState.new()
		state.register_agent(menu_assignment())
		result = state.register_agent(menu_assignment())
		self.assertEqual("BLOCKED", result.status)

	def test_reader_does_not_consume_writer_slot(self):
		state = team.TeamState.new()
		state.register_agent(menu_assignment())
		state.register_agent(point_assignment())
		reader = team.AgentAssignment(
			agent_id="reader-1",
			role="Review",
			worktree_path="../wt-review",
			owned_paths=("docs/**",),
			is_writer=False,
		)
		result = state.register_agent(reader)
		self.assertEqual("REGISTERED", result.status)
		self.assertEqual(3, state.active_count())
		self.assertEqual(2, state.writer_count())

	def test_release_frees_slot_for_reuse(self):
		state = team.TeamState.new()
		state.register_agent(menu_assignment())
		state.register_agent(point_assignment())
		self.assertTrue(state.release_agent("dev-menu"))
		third = team.AgentAssignment(
			agent_id="dev-order",
			role="Dev",
			worktree_path="../wt-order",
			owned_paths=("src/main/java/com/example/coffeeordersystem/order/**",),
		)
		result = state.register_agent(third)
		self.assertEqual("REGISTERED", result.status)

	def test_release_unknown_agent_returns_false(self):
		state = team.TeamState.new()
		self.assertFalse(state.release_agent("nobody"))


class MessageProtocolTest(unittest.TestCase):
	def test_only_four_message_types_are_allowed(self):
		self.assertEqual({"FINDING", "NEED", "BLOCKED", "SCOPE_CONFLICT"}, set(team.MESSAGE_TYPES))

	def test_valid_message_types_are_accepted(self):
		state = team.TeamState.new()
		state.register_agent(menu_assignment())
		for message_type in team.MESSAGE_TYPES:
			with self.subTest(message_type=message_type):
				state.send_message("dev-menu", message_type, "테스트 메시지")
		self.assertEqual(4, len(state.messages))

	def test_arbitrary_free_text_message_type_is_rejected(self):
		state = team.TeamState.new()
		state.register_agent(menu_assignment())
		with self.assertRaises(team.TeamOrchestrationError):
			state.send_message("dev-menu", "STATUS_UPDATE", "임의 자유 메시지")

	def test_message_from_unregistered_agent_is_rejected(self):
		state = team.TeamState.new()
		with self.assertRaises(team.TeamOrchestrationError):
			state.send_message("ghost", "FINDING", "등록 안 된 에이전트")

	def test_scope_conflict_message_increments_out_of_scope_metric(self):
		state = team.TeamState.new()
		state.register_agent(menu_assignment())
		state.send_message("dev-menu", "SCOPE_CONFLICT", "다른 agent와 경로 충돌")
		self.assertEqual(1, state.metrics_snapshot()["out_of_scope_changes"])


class StatePersistenceTest(unittest.TestCase):
	def test_round_trip_preserves_assignments_and_messages(self):
		state = team.TeamState.new()
		state.register_agent(menu_assignment())
		state.send_message("dev-menu", "FINDING", "라운드트립 확인")

		with tempfile.TemporaryDirectory() as temp_dir:
			repository_root = Path(temp_dir)
			team.save_state(repository_root, state)
			loaded = team.load_state(repository_root)

		self.assertEqual(1, loaded.active_count())
		self.assertEqual(1, len(loaded.messages))
		self.assertIn("dev-menu", loaded.assignments)

	def test_state_file_lives_under_the_gitignored_directory(self):
		with tempfile.TemporaryDirectory() as temp_dir:
			repository_root = Path(temp_dir)
			path = team.state_file_path(repository_root)
		self.assertEqual(team.DEFAULT_STATE_DIR, path.parent.name)

	def test_gitignored_state_directory_is_declared_in_repository_gitignore(self):
		repository_root = Path(__file__).resolve().parents[2]
		gitignore = (repository_root / ".gitignore").read_text(encoding="utf-8")
		self.assertIn(team.DEFAULT_STATE_DIR, gitignore)

	def test_load_state_without_existing_file_returns_fresh_state(self):
		with tempfile.TemporaryDirectory() as temp_dir:
			state = team.load_state(Path(temp_dir))
		self.assertEqual(0, state.active_count())


class NoAutoMergeInvariantTest(unittest.TestCase):
	"""Issue #91: '자동 branch 통합, 자동 merge, 무한 autofix를 금지한다' is a structural
	invariant, not a runtime check — this module must not define any merge/integration capability."""

	FORBIDDEN_TOKENS = ("merge", "rebase", "cherry-pick", "cherry_pick", "autofix", "auto_fix")

	def test_source_defines_no_merge_or_autofix_functions(self):
		source = inspect.getsource(team)
		function_names = [
			name
			for name, obj in inspect.getmembers(team)
			if inspect.isfunction(obj) and obj.__module__ == team.__name__
		]
		for name in function_names:
			for token in self.FORBIDDEN_TOKENS:
				self.assertNotIn(
					token,
					name.lower(),
					f"function '{name}' must not implement merge/autofix behavior.",
				)
		# Also scan the raw source for git merge-integration subcommands, in case a future
		# edit shells out to git instead of defining a named function.
		self.assertNotIn("git\", \"merge", source)
		self.assertNotIn("git merge", source)

	def test_metrics_snapshot_merge_conflicts_is_always_zero(self):
		state = team.TeamState.new()
		state.register_agent(menu_assignment())
		state.register_agent(point_assignment())
		state.send_message("dev-menu", "SCOPE_CONFLICT", "충돌 보고")
		self.assertEqual(0, state.metrics_snapshot()["merge_conflicts"])


class SyntheticSmokeTest(unittest.TestCase):
	"""Issue #91 완료 기준: 'coffee 합성 독립 작업 2개로 smoke가 통과한다(속도 주장 아님)'.

	이 테스트는 이 도구의 기능이 안전하게 동작하는지만 확인하며, 실제 속도·토큰 효과의 증거로
	쓰지 않는다(설계 문서·evidence에도 같은 문구로 명시한다).
	"""

	def test_two_non_overlapping_synthetic_dev_tasks_run_end_to_end(self):
		state = team.TeamState.new()

		# 두 개의 독립 합성 작업: menu 모듈, point 모듈 (경로 안 겹침).
		result_a = state.register_agent(menu_assignment(agent_id="synthetic-menu-dev"))
		result_b = state.register_agent(point_assignment(agent_id="synthetic-point-dev"))
		self.assertEqual("REGISTERED", result_a.status)
		self.assertEqual("REGISTERED", result_b.status)

		state.send_message("synthetic-menu-dev", "NEED", "menu DTO 확정 필요")
		state.send_message("synthetic-point-dev", "FINDING", "point 잔액 검증 로직 확인")
		state.send_message("synthetic-menu-dev", "BLOCKED", "공용 상수 정의 대기")

		self.assertEqual(2, state.active_count())
		self.assertEqual(2, state.writer_count())
		self.assertEqual(3, len(state.messages))

		self.assertTrue(state.release_agent("synthetic-menu-dev"))
		self.assertTrue(state.release_agent("synthetic-point-dev"))
		self.assertEqual(0, state.active_count())

		metrics = state.metrics_snapshot()
		expected_keys = {
			"wall_clock_seconds",
			"agent_count",
			"token_measurable",
			"retries",
			"stalls",
			"out_of_scope_changes",
			"review_qa_defects",
			"merge_conflicts",
			"human_interventions",
		}
		self.assertEqual(expected_keys, set(metrics.keys()))
		self.assertEqual(0, metrics["merge_conflicts"])
		self.assertEqual(0, metrics["human_interventions"])

	def test_two_synthetic_tasks_that_share_a_module_produce_scope_conflict(self):
		# 안전장치 확인용 반례: 겹치는 경로를 가진 합성 작업 2개는 등록 시점에 막혀야 한다.
		state = team.TeamState.new()
		state.register_agent(menu_assignment(agent_id="synthetic-menu-dev-1"))
		overlapping = team.AgentAssignment(
			agent_id="synthetic-menu-dev-2",
			role="Dev",
			worktree_path="../wt-menu-2",
			owned_paths=("src/main/java/com/example/coffeeordersystem/menu/controller/**",),
		)
		result = state.register_agent(overlapping)
		self.assertEqual("SCOPE_CONFLICT", result.status)


if __name__ == "__main__":
	unittest.main()
