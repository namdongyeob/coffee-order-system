# 하네스 품질 게이트의 성공과 실패 조건을 검증하는 테스트
import io
import tempfile
import unittest
from contextlib import redirect_stdout
from pathlib import Path

from scripts import harness_gate


VALID_ACCEPTANCE = """# Issue #23 Acceptance Criteria

Execution mode: STRICT
Execution mode reason: 하네스와 워크플로 정책을 변경하는 작업이므로 독립 Review와 QA가 필요합니다.
Level 5 required: NO
Level 5 reason: 애플리케이션 동작을 변경하지 않는 저장소 운영 작업입니다.
Level 6 required: NO
Level 6 reason: 호출할 API가 없는 저장소 운영 작업입니다.
"""

VALID_PR_BODY = """# Pull Request

Execution mode: STRICT
Execution mode reason: 하네스와 워크플로 정책을 변경하므로 STRICT 검증이 필요합니다.
"""

VALID_ATTEMPT = """# Attempt Log

Issue: #23
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/23
Branch: codex/issue-23-harness-quality-gates

## Attempt 1
### Generate
- 검사 스크립트를 작성했습니다.
### Evaluate
- PASS
### Failure Cause
- 없음
### Change Scope
- scripts만 변경했습니다.
### Reverification
- unittest PASS
### Next Attempt
- 없음
"""

VERIFICATION_HEADER = """# 검증 로그

| 날짜 | Issue | Level | 결과 | 검증 범위 | 명령/Evidence | 비고 |
| --- | --- | --- | --- | --- | --- | --- |
"""

LEGACY_VERIFICATION_IDENTIFIERS = {
	("project bootstrap inspection", "Level 1", "`./gradlew.bat test`"),
	("dependency and docs audit", "Level 1", "`./gradlew.bat test`"),
	(
		"Issue #2 project standards",
		"Level 0",
		'`rg -n "issue-completion-checklist|agent-mistakes|verification-log|layered-design-policy" AGENTS.md .github docs`',
	),
	("Issue #2 project standards", "Level 1", "`./gradlew.bat test`"),
	(
		"Issue #3 Flyway schema",
		"Level 3",
		"`./gradlew.bat test --tests com.example.coffeeordersystem.DatabaseSchemaIntegrationTest`",
	),
	("Issue #3 Flyway schema", "Level 1", "`./gradlew.bat test`"),
	(
		"Issue #4 menu list API",
		"Level 2",
		"`./gradlew.bat clean test --tests com.example.coffeeordersystem.menu.controller.MenuControllerTest --no-daemon`",
	),
	("Issue #4 menu list API", "Level 1", "`./gradlew.bat test --no-daemon`"),
	(
		"Issue #5 point charge API",
		"Level 2",
		"`./gradlew.bat clean test --tests com.example.coffeeordersystem.point.controller.PointControllerTest --tests com.example.coffeeordersystem.PointChargeIntegrationTest --no-daemon`",
	),
	(
		"Issue #5 point charge API",
		"Level 3",
		"`./gradlew.bat clean test --tests com.example.coffeeordersystem.point.controller.PointControllerTest --tests com.example.coffeeordersystem.PointChargeIntegrationTest --no-daemon`",
	),
	("Issue #5 point charge API", "Level 1", "`./gradlew.bat test --no-daemon`"),
	(
		"Issue #6 order payment API",
		"Level 2",
		"`./gradlew.bat clean test --tests com.example.coffeeordersystem.order.controller.OrderControllerTest --tests com.example.coffeeordersystem.OrderPaymentIntegrationTest --no-daemon`",
	),
	(
		"Issue #6 order payment API",
		"Level 3",
		"`./gradlew.bat clean test --tests com.example.coffeeordersystem.order.controller.OrderControllerTest --tests com.example.coffeeordersystem.OrderPaymentIntegrationTest --no-daemon`",
	),
	(
		"Issue #6 order payment concurrency fix",
		"Level 3",
		"`./gradlew.bat test --tests com.example.coffeeordersystem.OrderPaymentIntegrationTest --no-daemon`",
	),
	(
		"Issue #6 related point regression",
		"Level 2",
		"`./gradlew.bat test --tests com.example.coffeeordersystem.point.controller.PointControllerTest --tests com.example.coffeeordersystem.PointChargeIntegrationTest --no-daemon`",
	),
	(
		"Issue #6 related point regression",
		"Level 3",
		"`./gradlew.bat test --tests com.example.coffeeordersystem.point.controller.PointControllerTest --tests com.example.coffeeordersystem.PointChargeIntegrationTest --no-daemon`",
	),
	("Issue #6 order payment API", "Level 1", "`./gradlew.bat test --no-daemon`"),
	(
		"Issue #23 harness quality gates",
		"Level 0",
		'`python -m unittest discover -s scripts/tests -p "test_*.py"`',
	),
	(
		"Issue #23 harness repository gate",
		"Level 0",
		"`python scripts/harness_gate.py --issue 23 --base-ref origin/main --check-links`",
	),
	(
		"Issue #23 Git hooks",
		"Level 0",
		"`git hook run pre-commit`, `git hook run pre-push`",
	),
	("Issue #23 Java compile", "Level 1", "`.\\gradlew.bat compileJava --no-daemon`"),
	("Issue #23 full regression", "Level 1", "`.\\gradlew.bat test --no-daemon`"),
	("Issue #23 final regression", "Level 1", "`.\\gradlew.bat test --no-daemon`"),
	(
		"Issue #23 coordinator-only follow-up",
		"Level 0",
		"QA Agent가 하네스 테스트, repository gate, diff check 실행",
	),
	(
		"Issue #23 adaptive orchestration",
		"Level 0",
		"QA Agent의 28건 테스트, PR body validation, harness, diff, YAML 검증과 Reviewer 확인",
	),
}


def verification_log(*rows: str) -> str:
	return VERIFICATION_HEADER + "\n".join(rows) + "\n"


def write_issue_evidence(
	root: Path,
	acceptance: str,
	verification: str,
	issue: int = 23,
) -> None:
	evidence = root / "docs" / "testing" / "evidence" / f"issue-{issue}"
	evidence.mkdir(parents=True)
	(evidence / "acceptance-criteria.md").write_text(acceptance, encoding="utf-8")
	(evidence / "attempt-log.md").write_text(VALID_ATTEMPT, encoding="utf-8")
	(evidence / "commands.md").write_text("# Commands\n- unittest: PASS\n", encoding="utf-8")
	(evidence / "manual-qa.md").write_text("# Manual QA\n- harness: PASS\n", encoding="utf-8")
	verification_path = root / "docs" / "testing" / "verification-log.md"
	verification_path.write_text(verification, encoding="utf-8")


class BranchGuardTest(unittest.TestCase):
	def test_main_branch_is_blocked(self):
		self.assertFalse(harness_gate.branch_is_allowed("main"))

	def test_master_branch_is_blocked(self):
		self.assertFalse(harness_gate.branch_is_allowed("master"))

	def test_feature_branch_is_allowed(self):
		self.assertTrue(harness_gate.branch_is_allowed("codex/issue-23-harness-quality-gates"))


class EvidenceValidationTest(unittest.TestCase):
	def test_level_5_or_6_declaration_is_required(self):
		errors = harness_gate.validate_acceptance_criteria("Level 5 required: NO")
		self.assertTrue(any("Level 6 required" in error for error in errors))

	def test_level_reason_is_required(self):
		content = VALID_ACCEPTANCE.replace(
			"Level 5 reason: 애플리케이션 동작을 변경하지 않는 저장소 운영 작업입니다.\n",
			"Level 5 reason:\n",
		)
		errors = harness_gate.validate_acceptance_criteria(content)
		self.assertTrue(any("Level 5 reason" in error for error in errors))

	def test_execution_mode_is_required(self):
		content = VALID_ACCEPTANCE.replace("Execution mode: STRICT\n", "")

		errors = harness_gate.validate_acceptance_criteria(content)

		self.assertTrue(any("Execution mode:" in error for error in errors))

	def test_execution_mode_must_be_solo_standard_or_strict(self):
		content = VALID_ACCEPTANCE.replace("Execution mode: STRICT", "Execution mode: FAST")

		errors = harness_gate.validate_acceptance_criteria(content)

		self.assertTrue(any("Execution mode: SOLO|STANDARD|STRICT" in error for error in errors))

	def test_execution_mode_reason_is_required(self):
		content = VALID_ACCEPTANCE.replace(
			"Execution mode reason: 하네스와 워크플로 정책을 변경하는 작업이므로 독립 Review와 QA가 필요합니다.\n",
			"Execution mode reason:\n",
		)

		errors = harness_gate.validate_acceptance_criteria(content)

		self.assertTrue(any("Execution mode reason" in error for error in errors))

	def test_duplicate_execution_mode_is_rejected(self):
		content = VALID_ACCEPTANCE + "Execution mode: SOLO\n"

		errors = harness_gate.validate_acceptance_criteria(content)

		self.assertTrue(any("duplicate Execution mode" in error for error in errors))

	def test_duplicate_execution_mode_reason_is_rejected(self):
		content = VALID_ACCEPTANCE + "Execution mode reason: 두 번째 근거입니다.\n"

		errors = harness_gate.validate_acceptance_criteria(content)

		self.assertTrue(any("duplicate Execution mode reason" in error for error in errors))

	def test_attempt_log_sections_are_required(self):
		errors = harness_gate.validate_attempt_log("## Attempt 1\n### Generate\n")
		self.assertTrue(any("### Evaluate" in error for error in errors))
		self.assertTrue(any("### Next Attempt" in error for error in errors))

	def test_attempt_log_issue_linkage_is_required(self):
		content = VALID_ATTEMPT.replace("Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/23\n", "")
		errors = harness_gate.validate_attempt_log(content)
		self.assertTrue(any("Issue URL" in error for error in errors))

	def test_verification_log_must_reference_issue(self):
		errors = harness_gate.validate_verification_log(
			verification_log(
				"| 2026-07-10 | Issue #22 | Level 0 | PASS | harness | `python -m unittest` | 완료 |"
			),
			23,
		)
		self.assertEqual(["verification-log.md에 Issue #23 기록이 없습니다."], errors)

	def test_verification_log_requires_result_row(self):
		errors = harness_gate.validate_verification_log("Issue #23", 23)
		self.assertTrue(any("Issue #23 결과 행이 없습니다" in error for error in errors))

	def test_empty_commands_and_manual_qa_fail(self):
		with tempfile.TemporaryDirectory() as temp_dir:
			root = Path(temp_dir)
			evidence = root / "docs" / "testing" / "evidence" / "issue-23"
			evidence.mkdir(parents=True)
			(evidence / "acceptance-criteria.md").write_text(VALID_ACCEPTANCE, encoding="utf-8")
			(evidence / "attempt-log.md").write_text(VALID_ATTEMPT, encoding="utf-8")
			(evidence / "commands.md").write_text("# Commands\n", encoding="utf-8")
			(evidence / "manual-qa.md").write_text("# Manual QA\n", encoding="utf-8")
			verification = root / "docs" / "testing" / "verification-log.md"
			verification.write_text(
				verification_log(
					"| 2026-07-10 | Issue #23 | Level 0 | PASS | harness | `python -m unittest` | 완료 |"
				),
				encoding="utf-8",
			)

			errors = harness_gate.validate_issue_evidence(root, 23)

			self.assertTrue(any("commands.md" in error for error in errors))
			self.assertTrue(any("manual-qa.md" in error for error in errors))

	def test_valid_issue_evidence_passes(self):
		with tempfile.TemporaryDirectory() as temp_dir:
			root = Path(temp_dir)
			evidence = root / "docs" / "testing" / "evidence" / "issue-23"
			evidence.mkdir(parents=True)
			(evidence / "acceptance-criteria.md").write_text(VALID_ACCEPTANCE, encoding="utf-8")
			(evidence / "attempt-log.md").write_text(VALID_ATTEMPT, encoding="utf-8")
			(evidence / "commands.md").write_text(
				"# Commands\n- unittest: PASS\n", encoding="utf-8"
			)
			(evidence / "manual-qa.md").write_text(
				"# Manual QA\n- main branch guard: PASS\n", encoding="utf-8"
			)
			verification = root / "docs" / "testing" / "verification-log.md"
			verification.write_text(
				verification_log(
					"| 2026-07-10 | Issue #23 | Level 0 | PASS | harness | `python -m unittest` | 완료 |"
				),
				encoding="utf-8",
			)

			self.assertEqual([], harness_gate.validate_issue_evidence(root, 23))


class VerificationLogValidationTest(unittest.TestCase):
	def test_code_span_delimiter_length_preserves_internal_pipes(self):
		commands = (
			'`printf "single|pipe"`',
			'``printf "double|pipe"``',
			'```printf "triple|pipe"```',
			'````printf "quadruple|pipe"````',
		)
		for command in commands:
			with self.subTest(command=command):
				markdown = verification_log(
					f"| 2026-07-10 | Issue #23 | Level 0 | PASS | parser | {command} | 완료 |"
				)
				self.assertEqual([], harness_gate.validate_verification_log(markdown, 23))

	def test_windows_trailing_backslash_does_not_escape_code_span_closer(self):
		commands = (
			r'`dir C:\temp\`',
			r'``dir C:\temp\``',
		)
		for command in commands:
			with self.subTest(command=command):
				markdown = verification_log(
					f"| 2026-07-10 | Issue #23 | Level 0 | PASS | parser | {command} | 완료 |"
				)
				self.assertEqual([], harness_gate.validate_verification_log(markdown, 23))

	def test_escaped_pipe_is_not_a_column_separator(self):
		markdown = verification_log(
			"| 2026-07-10 | Issue #23 | Level 0 | PASS | parser | printf a\\|b | 완료 |"
		)

		self.assertEqual([], harness_gate.validate_verification_log(markdown, 23))

	def test_levels_zero_through_seven_are_valid(self):
		for level in range(8):
			with self.subTest(level=level):
				markdown = verification_log(
					f"| 2026-07-10 | Issue #23 | Level {level} | PASS | 범위 | `command` | 비고 |"
				)
				self.assertEqual([], harness_gate.validate_verification_log(markdown, 23))

	def test_out_of_range_free_form_and_composite_levels_are_invalid(self):
		for level in ("Level 8", "Level 1 smoke", "Level 2 + Level 3"):
			with self.subTest(level=level):
				markdown = verification_log(
					f"| 2026-07-10 | Issue #23 | {level} | PASS | 범위 | `command` | 비고 |"
				)
				errors = harness_gate.validate_verification_log(markdown, 23)
				self.assertTrue(any(f"invalid Level '{level}'" in error for error in errors))

	def test_result_must_be_pass_fail_or_partial(self):
		markdown = verification_log(
			"| 2026-07-10 | Issue #23 | Level 0 | SUCCESS | 범위 | `command` | 비고 |"
		)

		errors = harness_gate.validate_verification_log(markdown, 23)

		self.assertTrue(any("invalid 결과 'SUCCESS'" in error for error in errors))

	def test_fail_and_partial_are_valid_log_results(self):
		for result in ("FAIL", "PARTIAL"):
			with self.subTest(result=result):
				markdown = verification_log(
					f"| 2026-07-10 | Issue #23 | Level 0 | {result} | 범위 | `command` | 비고 |"
				)
				self.assertEqual([], harness_gate.validate_verification_log(markdown, 23))

	def test_every_data_row_must_have_seven_columns(self):
		markdown = verification_log(
			"| 2026-07-10 | Issue #23 | Level 0 | PASS | 범위 | `command` |"
		)

		errors = harness_gate.validate_verification_log(markdown, 23)

		self.assertTrue(any("expected 7 columns" in error for error in errors))

	def test_required_level_pass_for_same_issue_succeeds(self):
		acceptance = VALID_ACCEPTANCE.replace("Level 5 required: NO", "Level 5 required: YES")
		log = verification_log(
			"| 2026-07-10 | Issue #23 | Level 5 | PASS | 로컬 앱 기동 | `command` | 완료 |"
		)
		with tempfile.TemporaryDirectory() as temp_dir:
			root = Path(temp_dir)
			write_issue_evidence(root, acceptance, log)
			self.assertEqual([], harness_gate.validate_issue_evidence(root, 23))

	def test_required_level_fails_when_pass_is_missing(self):
		acceptance = VALID_ACCEPTANCE.replace("Level 5 required: NO", "Level 5 required: YES")
		log = verification_log(
			"| 2026-07-10 | Issue #23 | Level 0 | PASS | harness | `command` | 완료 |"
		)
		with tempfile.TemporaryDirectory() as temp_dir:
			root = Path(temp_dir)
			write_issue_evidence(root, acceptance, log)
			errors = harness_gate.validate_issue_evidence(root, 23)
			self.assertTrue(any("Issue #23 required Level 5 PASS" in error for error in errors))

	def test_required_level_rejects_wrong_issue_wrong_level_and_partial(self):
		acceptance = VALID_ACCEPTANCE.replace("Level 5 required: NO", "Level 5 required: YES")
		rows = (
			"| 2026-07-10 | Issue #22 | Level 5 | PASS | 범위 | `command` | 비고 |",
			"| 2026-07-10 | Issue #23 | Level 6 | PASS | 범위 | `command` | 비고 |",
			"| 2026-07-10 | Issue #23 | Level 5 | PARTIAL | 범위 | `command` | 비고 |",
		)
		for row in rows:
			with self.subTest(row=row):
				with tempfile.TemporaryDirectory() as temp_dir:
					root = Path(temp_dir)
					write_issue_evidence(root, acceptance, verification_log(row))
					errors = harness_gate.validate_issue_evidence(root, 23)
					self.assertTrue(any("Issue #23 required Level 5 PASS" in error for error in errors))

	def test_required_level_no_does_not_require_pass(self):
		log = verification_log(
			"| 2026-07-10 | Issue #23 | Level 0 | PASS | harness | `command` | 완료 |"
		)
		with tempfile.TemporaryDirectory() as temp_dir:
			root = Path(temp_dir)
			write_issue_evidence(root, VALID_ACCEPTANCE, log)
			self.assertEqual([], harness_gate.validate_issue_evidence(root, 23))

	def test_required_level_6_pass_for_same_issue_succeeds(self):
		acceptance = VALID_ACCEPTANCE.replace("Level 6 required: NO", "Level 6 required: YES")
		log = verification_log(
			"| 2026-07-10 | Issue #23 | Level 6 | PASS | 실제 HTTP | `command` | 완료 |"
		)
		with tempfile.TemporaryDirectory() as temp_dir:
			root = Path(temp_dir)
			write_issue_evidence(root, acceptance, log)
			self.assertEqual([], harness_gate.validate_issue_evidence(root, 23))

	def test_required_level_6_rejects_missing_wrong_issue_wrong_level_and_partial(self):
		acceptance = VALID_ACCEPTANCE.replace("Level 6 required: NO", "Level 6 required: YES")
		rows = (
			"| 2026-07-10 | Issue #23 | Level 0 | PASS | 범위 | `command` | 비고 |",
			"| 2026-07-10 | Issue #22 | Level 6 | PASS | 범위 | `command` | 비고 |",
			"| 2026-07-10 | Issue #23 | Level 5 | PASS | 범위 | `command` | 비고 |",
			"| 2026-07-10 | Issue #23 | Level 6 | PARTIAL | 범위 | `command` | 비고 |",
		)
		for row in rows:
			with self.subTest(row=row):
				with tempfile.TemporaryDirectory() as temp_dir:
					root = Path(temp_dir)
					write_issue_evidence(root, acceptance, verification_log(row))
					errors = harness_gate.validate_issue_evidence(root, 23)
					self.assertTrue(any("Issue #23 required Level 6 PASS" in error for error in errors))

	def test_repository_verification_log_is_normalized(self):
		repository_root = Path(__file__).resolve().parents[2]
		markdown = (repository_root / "docs" / "testing" / "verification-log.md").read_text(
			encoding="utf-8"
		)

		self.assertEqual([], harness_gate.validate_verification_log(markdown, 23))

	def test_repository_keeps_legacy_verification_identifiers(self):
		repository_root = Path(__file__).resolve().parents[2]
		markdown = (repository_root / "docs" / "testing" / "verification-log.md").read_text(
			encoding="utf-8"
		)
		rows, errors = harness_gate._verification_rows(markdown)
		actual_identifiers = {
			(row["Issue"], row["Level"], row["명령/Evidence"])
			for row in rows
		}

		self.assertEqual([], errors)
		self.assertEqual(25, len(LEGACY_VERIFICATION_IDENTIFIERS))
		self.assertEqual(set(), LEGACY_VERIFICATION_IDENTIFIERS - actual_identifiers)

	def test_level_1_smoke_does_not_replace_focused_evidence(self):
		repository_root = Path(__file__).resolve().parents[2]
		strategy = (repository_root / "docs" / "testing" / "test-strategy.md").read_text(
			encoding="utf-8"
		)

		self.assertIn("Level 1 전체 회귀 smoke는 전체 suite 상태를 기록", strategy)
		self.assertIn("Level 2, Level 3, Level 4의 focused evidence를 대체하지 않습니다", strategy)


class PullRequestBodyValidationTest(unittest.TestCase):
	def test_valid_pr_body_passes(self):
		self.assertEqual([], harness_gate.validate_pr_body(VALID_PR_BODY))

	def test_pr_body_requires_execution_mode_reason(self):
		content = VALID_PR_BODY.replace(
			"Execution mode reason: 하네스와 워크플로 정책을 변경하므로 STRICT 검증이 필요합니다.\n",
			"",
		)

		errors = harness_gate.validate_pr_body(content)

		self.assertTrue(any("Execution mode reason" in error for error in errors))

	def test_pr_body_file_cli_rejects_invalid_body_without_network(self):
		with tempfile.TemporaryDirectory() as temp_dir:
			body_path = Path(temp_dir) / "pr-body.md"
			body_path.write_text("Execution mode: FAST\n", encoding="utf-8")
			output = io.StringIO()

			with redirect_stdout(output):
				result = harness_gate.main(
					[
						"--branch",
						"issue-23-test",
						"--check-branch",
						"--pr-body-file",
						str(body_path),
					]
				)

			self.assertEqual(1, result)
			self.assertIn("Execution mode", output.getvalue())


class MarkdownLinkTest(unittest.TestCase):
	def test_repository_context_router_declared_paths_pass(self):
		repository_root = Path(__file__).resolve().parents[2]

		self.assertEqual([], harness_gate.validate_context_router_paths(repository_root))

	def test_context_router_paths_pass_when_every_declared_path_exists(self):
		with tempfile.TemporaryDirectory() as temp_dir:
			root = Path(temp_dir)
			router = root / "docs" / "ai" / "context-router.md"
			target = root / "docs" / "testing" / "evidence-guide.md"
			router.parent.mkdir(parents=True)
			target.parent.mkdir(parents=True)
			router.write_text(
				"[Evidence guide](../testing/evidence-guide.md)\n", encoding="utf-8"
			)
			target.write_text("# Evidence guide\n", encoding="utf-8")

			self.assertEqual([], harness_gate.validate_context_router_paths(root))

	def test_context_router_paths_fail_when_a_declared_path_is_missing(self):
		with tempfile.TemporaryDirectory() as temp_dir:
			root = Path(temp_dir)
			router = root / "docs" / "ai" / "context-router.md"
			router.parent.mkdir(parents=True)
			router.write_text("[Missing](missing.md)\n", encoding="utf-8")

			errors = harness_gate.validate_context_router_paths(root)

			self.assertEqual(1, len(errors))
			self.assertIn("missing.md", errors[0])

	def test_broken_relative_link_fails(self):
		with tempfile.TemporaryDirectory() as temp_dir:
			root = Path(temp_dir)
			doc = root / "docs" / "guide.md"
			doc.parent.mkdir(parents=True)
			doc.write_text("[missing](missing.md)", encoding="utf-8")

			errors = harness_gate.validate_markdown_links(root, [doc])

			self.assertEqual(1, len(errors))
			self.assertIn("missing.md", errors[0])


class OrchestrationContractTest(unittest.TestCase):
	def test_execution_modes_keep_sources_separated(self):
		repository_root = Path(__file__).resolve().parents[2]
		skill = (
			repository_root / ".codex" / "skills" / "coffee-order-issue-loop" / "SKILL.md"
		).read_text(encoding="utf-8")
		policy = (repository_root / "docs" / "ai" / "orchestration-policy.md").read_text(
			encoding="utf-8"
		)
		test_strategy = (repository_root / "docs" / "testing" / "test-strategy.md").read_text(
			encoding="utf-8"
		)

		self.assertIn("Execution mode: SOLO|STANDARD|STRICT", policy)
		self.assertIn("독립 Combined Verifier", policy)
		self.assertIn("Dev 동시성의 최대치는 2", policy)
		self.assertIn("실행 모드 선택과 역할 구성은 `docs/ai/orchestration-policy.md`", test_strategy)
		self.assertIn("BLOCKED: EXECUTION MODE REQUIRED", skill)
		self.assertIn("BLOCKED: COORDINATOR ONLY", skill)
		self.assertIn("BLOCKED: DEV CONCURRENCY LIMIT", skill)
		self.assertNotIn("동시성은 2", skill)
		self.assertNotIn("Solo Agent 한 명", skill)
		self.assertIn("전체 대화 fork 없이", skill)
		self.assertIn("같은 Dev에게 한 번만", skill)
		self.assertIn("BLOCKED: AGENT STALLED", skill)
		self.assertIn("docs/testing/test-strategy.md", skill)
		self.assertNotIn("역할과 쓰기 권한:", skill)

	def test_policy_is_the_single_execution_mode_contract(self):
		repository_root = Path(__file__).resolve().parents[2]
		policy = (repository_root / "docs" / "ai" / "orchestration-policy.md").read_text(
			encoding="utf-8"
		)
		agent_rules = (repository_root / "docs" / "ai" / "agent-rules.md").read_text(
			encoding="utf-8"
		)

		self.assertRegex(policy, r"`SOLO`.*문서 전용.*Solo Agent 한 명")
		self.assertRegex(policy, r"`STANDARD`.*Dev Agent.*Combined Verifier.*CI")
		self.assertNotRegex(policy, r"`STANDARD`[^\n]*Docs Agent")
		self.assertRegex(
			policy, r"`STRICT`.*Dev Agent.*Review Agent.*QA Agent.*Docs Agent.*CI"
		)
		self.assertIn("실행 모드별 역할 구성은 `docs/ai/orchestration-policy.md`", agent_rules)
		self.assertNotIn("Combined Verifier", agent_rules)

	def test_pull_request_workflow_validates_body_from_temp_file(self):
		repository_root = Path(__file__).resolve().parents[2]
		workflow = (repository_root / ".github" / "workflows" / "harness-quality.yml").read_text(
			encoding="utf-8"
		)

		self.assertIn("PR_BODY: ${{ github.event.pull_request.body }}", workflow)
		self.assertIn("printf '%s' \"$PR_BODY\" > \"$RUNNER_TEMP/pr-body.md\"", workflow)
		self.assertIn('--pr-body-file "$RUNNER_TEMP/pr-body.md"', workflow)

	def test_issue_and_pr_templates_include_execution_mode_fields(self):
		repository_root = Path(__file__).resolve().parents[2]
		templates = [
			*(repository_root / ".github" / "ISSUE_TEMPLATE").glob("*.md"),
			repository_root / ".github" / "PULL_REQUEST_TEMPLATE.md",
		]

		for template in templates:
			content = template.read_text(encoding="utf-8")
			with self.subTest(template=template.name):
				self.assertRegex(content, r"(?m)^Execution mode: (SOLO|STANDARD|STRICT)$")
				self.assertRegex(content, r"(?m)^Execution mode reason: \S.+$")

	def test_existing_relative_link_passes(self):
		with tempfile.TemporaryDirectory() as temp_dir:
			root = Path(temp_dir)
			doc = root / "docs" / "guide.md"
			target = root / "docs" / "target.md"
			doc.parent.mkdir(parents=True)
			doc.write_text("[target](target.md)", encoding="utf-8")
			target.write_text("# Target", encoding="utf-8")

			self.assertEqual([], harness_gate.validate_markdown_links(root, [doc]))

	def test_parentheses_in_relative_link_pass(self):
		with tempfile.TemporaryDirectory() as temp_dir:
			root = Path(temp_dir)
			doc = root / "docs" / "guide.md"
			target = root / "docs" / "target(1).md"
			doc.parent.mkdir(parents=True)
			doc.write_text("[target](target(1).md)", encoding="utf-8")
			target.write_text("# Target", encoding="utf-8")

			self.assertEqual([], harness_gate.validate_markdown_links(root, [doc]))

	def test_reference_style_link_passes(self):
		with tempfile.TemporaryDirectory() as temp_dir:
			root = Path(temp_dir)
			doc = root / "docs" / "guide.md"
			target = root / "docs" / "target.md"
			doc.parent.mkdir(parents=True)
			doc.write_text("[target][ref]\n\n[ref]: target.md", encoding="utf-8")
			target.write_text("# Target", encoding="utf-8")

			self.assertEqual([], harness_gate.validate_markdown_links(root, [doc]))

	def test_missing_reference_style_link_fails(self):
		with tempfile.TemporaryDirectory() as temp_dir:
			root = Path(temp_dir)
			doc = root / "docs" / "guide.md"
			doc.parent.mkdir(parents=True)
			doc.write_text("[target][ref]\n\n[ref]: missing.md", encoding="utf-8")

			errors = harness_gate.validate_markdown_links(root, [doc])

			self.assertEqual(1, len(errors))
			self.assertIn("missing.md", errors[0])


if __name__ == "__main__":
	unittest.main()
