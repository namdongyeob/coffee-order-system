# 하네스 품질 게이트의 성공과 실패 조건을 검증하는 테스트
import io
import subprocess
import tempfile
import unittest
from contextlib import redirect_stdout
from pathlib import Path

from scripts import harness_gate


VALID_ACCEPTANCE = """# Issue #23 Acceptance Criteria

- [x] harness evidence reconciliation fixture

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

VALID_METRICS = """# Issue Metrics

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 4 | 12 | 0 | 0 | 0 | 0 | 0 | 4 |
"""

VALID_ATTEMPT = """# Attempt Log

Issue: #23
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/23
Branch: codex/issue-23-harness-quality-gates
Current disposition: PASS
Current Attempt: 1
Current head: d1326bdc81d4b2b62c9b11eb0083e7da99ea1de8

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

Attempt: 1
Head: d1326bdc81d4b2b62c9b11eb0083e7da99ea1de8

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
	(evidence / "metrics.md").write_text(VALID_METRICS, encoding="utf-8")
	verification_path = evidence / "verification.md"
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
			verification = evidence / "verification.md"
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
			(evidence / "metrics.md").write_text(VALID_METRICS, encoding="utf-8")
			verification = evidence / "verification.md"
			verification.write_text(
				verification_log(
					"| 2026-07-10 | Issue #23 | Level 0 | PASS | harness | `python -m unittest` | 완료 |"
				),
				encoding="utf-8",
			)

			self.assertEqual([], harness_gate.validate_issue_evidence(root, 23))

	def test_blocked_current_disposition_rejects_pass_verification(self):
		with tempfile.TemporaryDirectory() as temp_dir:
			root = Path(temp_dir)
			write_issue_evidence(
				root,
				VALID_ACCEPTANCE,
				verification_log(
					"| 2026-07-13 | Issue #23 | Level 0 | PASS | harness | `python -m unittest` | completed |"
				),
			)
			attempt = root / "docs" / "testing" / "evidence" / "issue-23" / "attempt-log.md"
			attempt.write_text(
				VALID_ATTEMPT.replace("Current disposition: PASS", "Current disposition: BLOCKED"),
				encoding="utf-8",
			)

			errors = harness_gate.validate_issue_evidence(root, 23)

			self.assertTrue(any("BLOCKED" in error and "PASS" in error for error in errors))

	def test_matching_pass_attempt_verification_and_metrics_pass(self):
		with tempfile.TemporaryDirectory() as temp_dir:
			root = Path(temp_dir)
			write_issue_evidence(
				root,
				VALID_ACCEPTANCE,
				verification_log(
					"| 2026-07-13 | Issue #23 | Level 0 | PASS | harness | `python -m unittest` | completed |"
				),
			)

			self.assertEqual([], harness_gate.validate_issue_evidence(root, 23))

	def test_retry_count_or_current_attempt_or_head_mismatch_fails(self):
		with tempfile.TemporaryDirectory() as temp_dir:
			root = Path(temp_dir)
			write_issue_evidence(
				root,
				VALID_ACCEPTANCE,
				verification_log(
					"| 2026-07-13 | Issue #23 | Level 0 | PASS | harness | `python -m unittest` | completed |"
				),
			)
			evidence = root / "docs" / "testing" / "evidence" / "issue-23"
			(evidence / "metrics.md").write_text(
				VALID_METRICS.replace("| STRICT | 4 | 12 | 0 |", "| STRICT | 4 | 12 | 1 |"),
				encoding="utf-8",
			)
			(evidence / "verification.md").write_text(
				verification_log(
					"| 2026-07-13 | Issue #23 | Level 0 | PASS | harness | `python -m unittest` | completed |"
				).replace("Head: d1326bdc81d4b2b62c9b11eb0083e7da99ea1de8", "Head: 1111111111111111111111111111111111111111"),
				encoding="utf-8",
			)

			errors = harness_gate.validate_issue_evidence(root, 23)

			self.assertTrue(any("retry" in error.lower() or "head" in error.lower() for error in errors))

	def test_retry_count_mismatch_fails_without_other_metadata_mismatch(self):
		with tempfile.TemporaryDirectory() as temp_dir:
			root = Path(temp_dir)
			write_issue_evidence(
				root,
				VALID_ACCEPTANCE,
				verification_log(
					"| 2026-07-13 | Issue #23 | Level 0 | PASS | harness | `python -m unittest` | completed |"
				),
			)
			evidence = root / "docs" / "testing" / "evidence" / "issue-23"
			(evidence / "metrics.md").write_text(
				VALID_METRICS.replace("| STRICT | 4 | 12 | 0 |", "| STRICT | 4 | 12 | 1 |"),
				encoding="utf-8",
			)

			errors = harness_gate.validate_issue_evidence(root, 23)

			self.assertEqual(
				["evidence reconciliation: metrics retry count must equal Current Attempt minus one."],
				errors,
			)

	def test_current_attempt_must_match_verification_attempt(self):
		with tempfile.TemporaryDirectory() as temp_dir:
			root = Path(temp_dir)
			write_issue_evidence(
				root,
				VALID_ACCEPTANCE,
				verification_log(
					"| 2026-07-13 | Issue #23 | Level 0 | PASS | harness | `python -m unittest` | completed |"
				),
			)
			attempt = root / "docs" / "testing" / "evidence" / "issue-23" / "attempt-log.md"
			attempt.write_text(
				VALID_ATTEMPT.replace("Current Attempt: 1", "Current Attempt: 2").replace(
					"## Attempt 1", "## Attempt 2"
				),
				encoding="utf-8",
			)

			errors = harness_gate.validate_issue_evidence(root, 23)

			self.assertTrue(any("Current Attempt and verification.md Attempt" in error for error in errors))

	def test_verification_execution_head_mismatch_fails_without_other_metadata_mismatch(self):
		with tempfile.TemporaryDirectory() as temp_dir:
			root = Path(temp_dir)
			write_issue_evidence(
				root,
				VALID_ACCEPTANCE,
				verification_log(
					"| 2026-07-13 | Issue #23 | Level 0 | PASS | harness | `python -m unittest` | completed |"
				).replace("Head: d1326bdc81d4b2b62c9b11eb0083e7da99ea1de8", "Head: 1111111111111111111111111111111111111111"),
			)

			errors = harness_gate.validate_issue_evidence(root, 23)

			self.assertEqual(
				["evidence reconciliation: Current head and verification.md Head must match."],
				errors,
			)

	def test_unknown_execution_head_ancestor_fails(self):
		errors = harness_gate.validate_execution_head_delta(
			"1111111111111111111111111111111111111111", False, [], 23
		)

		self.assertEqual(
			["evidence reconciliation: execution head must be an ancestor of current Git HEAD."],
			errors,
		)

	def test_code_or_test_change_after_execution_head_fails(self):
		errors = harness_gate.validate_execution_head_delta(
			"d1326bdc81d4b2b62c9b11eb0083e7da99ea1de8",
			True,
			["scripts/harness_gate.py"],
			23,
		)

		self.assertEqual(
			["evidence reconciliation: changes after execution head must be limited to Issue #23 evidence files: scripts/harness_gate.py"],
			errors,
		)

	def test_evidence_only_delta_after_execution_head_passes(self):
		errors = harness_gate.validate_execution_head_delta(
			"d1326bdc81d4b2b62c9b11eb0083e7da99ea1de8",
			True,
			[
				"docs/testing/evidence/issue-23/attempt-log.md",
				"docs/testing/evidence/issue-23/verification.md",
			],
			23,
		)

		self.assertEqual([], errors)

	def test_metrics_is_required_and_isolated_to_the_current_issue(self):
		with tempfile.TemporaryDirectory() as temp_dir:
			root = Path(temp_dir)
			write_issue_evidence(
				root,
				VALID_ACCEPTANCE,
				verification_log(
					"| 2026-07-10 | Issue #23 | Level 0 | PASS | harness | `python -m unittest` | 완료 |"
				),
			)
			(root / "docs" / "testing" / "evidence" / "issue-23" / "metrics.md").unlink()
			(root / "docs" / "testing" / "evidence" / "issue-22").mkdir(parents=True)

			errors = harness_gate.validate_issue_evidence(root, 23)

			self.assertTrue(any("metrics.md" in error for error in errors))

	def test_metrics_requires_the_exact_template_table_and_numeric_cells(self):
		invalid_metrics = VALID_METRICS.replace("| STRICT | 4 | 12 | 0 | 0 | 0 | 0 | 0 | 4 |", "| STRICT | four | unknown | 0 | 0 | 0 | 0 | 0 | 4 |\n| STRICT | 4 | 1 | 0 | 0 | 0 | 0 | 0 | 4 |")

		errors = harness_gate.validate_metrics(invalid_metrics)

		self.assertTrue(any("exactly one" in error for error in errors))
		self.assertTrue(any("Agent 수" in error for error in errors))
		self.assertTrue(any("작업 시간(분)" in error for error in errors))

	def test_acceptance_and_metrics_execution_modes_must_match(self):
		metrics = VALID_METRICS.replace("| STRICT |", "| SOLO |")

		errors = harness_gate.validate_execution_mode_consistency(VALID_ACCEPTANCE, metrics)

		self.assertTrue(any("acceptance-criteria.md and metrics.md" in error for error in errors))


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

	def test_issue_evidence_requires_issue_local_verification_file(self):
		with tempfile.TemporaryDirectory() as temp_dir:
			root = Path(temp_dir)
			write_issue_evidence(
				root,
				VALID_ACCEPTANCE,
				verification_log(
					"| 2026-07-10 | Issue #23 | Level 0 | PASS | harness | `python -m unittest` | 완료 |"
				),
			)
			(root / "docs" / "testing" / "evidence" / "issue-23" / "verification.md").unlink()
			errors = harness_gate.validate_issue_evidence(root, 23)

			self.assertTrue(any("verification.md" in error for error in errors))

	def test_issue_local_verification_path_prevents_parallel_issue_file_conflicts(self):
		self.assertEqual(
			Path("docs/testing/evidence/issue-51/verification.md"),
			harness_gate.verification_file_path(51),
		)
		self.assertNotEqual(
			harness_gate.verification_file_path(51),
			harness_gate.verification_file_path(52),
		)

	def test_two_branch_fixture_has_no_common_verification_source_file(self):
		with tempfile.TemporaryDirectory() as temp_dir:
			repository = Path(temp_dir)
			def git(*args: str) -> str:
				return subprocess.run(
					["git", *args], cwd=repository, text=True, check=True,
					stdout=subprocess.PIPE, stderr=subprocess.PIPE,
				).stdout

			git("init", "--initial-branch=main")
			git("config", "user.email", "fixture@example.test")
			git("config", "user.name", "fixture")
			(repository / "README.md").write_text("fixture\n", encoding="utf-8")
			git("add", "README.md")
			git("commit", "-m", "fixture base")

			for issue in (51, 52):
				branch = f"issue-{issue}"
				git("switch", "-c", branch, "main")
				path = repository / harness_gate.verification_file_path(issue)
				path.parent.mkdir(parents=True, exist_ok=True)
				path.write_text(verification_log(
					f"| 2026-07-13 | Issue #{issue} | Level 0 | PASS | fixture | `command` | preserved |"
				), encoding="utf-8")
				git("add", path.relative_to(repository).as_posix())
				git("commit", "-m", f"issue {issue} verification")

			changed = [
				set(git("diff", "--name-only", f"main...issue-{issue}").splitlines())
				for issue in (51, 52)
			]
			self.assertEqual(set(), changed[0].intersection(changed[1]))

	def test_repository_verification_log_is_rebuildable_from_issue_sources(self):
		repository_root = Path(__file__).resolve().parents[2]
		markdown = harness_gate.rebuild_verification_log(repository_root)

		self.assertEqual([], harness_gate.validate_verification_log(markdown, 23))

	def test_repository_keeps_legacy_verification_identifiers_after_migration(self):
		repository_root = Path(__file__).resolve().parents[2]
		markdown = harness_gate.rebuild_verification_log(repository_root)
		rows, errors = harness_gate._verification_rows(markdown)
		actual_identifiers = {
			(row["Issue"], row["Level"], row["명령/Evidence"])
			for row in rows
		}

		self.assertEqual([], errors)
		self.assertEqual(25, len(LEGACY_VERIFICATION_IDENTIFIERS))
		self.assertEqual(set(), LEGACY_VERIFICATION_IDENTIFIERS - actual_identifiers)

	def test_migration_preserves_original_row_text_verbatim(self):
		from scripts.migrate_verification_log import migrate

		with tempfile.TemporaryDirectory() as temp_dir:
			root = Path(temp_dir)
			source = root / "docs" / "testing" / "verification-log.md"
			source.parent.mkdir(parents=True)
			issue_row = "| 2026-07-13 | Issue #51 | Level 0 | PASS | 범위 | `echo a|b` | 원문 보존 |"
			legacy_row = "| 2026-07-12 | bootstrap | Level 1 | PARTIAL | 범위 | `command` | legacy |"
			source.write_text(verification_log(issue_row, legacy_row), encoding="utf-8")

			migrate(root, source)

			self.assertIn(
				issue_row,
				(root / "docs" / "testing" / "evidence" / "issue-51" / "verification.md").read_text(encoding="utf-8"),
			)
			self.assertIn(
				legacy_row,
				(root / "docs" / "testing" / "evidence" / "legacy" / "verification.md").read_text(encoding="utf-8"),
			)

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

	def test_extract_execution_mode_returns_only_one_valid_declaration(self):
		self.assertEqual("STRICT", harness_gate.extract_execution_mode(VALID_PR_BODY))
		self.assertIsNone(harness_gate.extract_execution_mode("Execution mode: FAST\n"))
		self.assertIsNone(harness_gate.extract_execution_mode(VALID_PR_BODY + "Execution mode: SOLO\n"))

	def test_pr_body_execution_mode_must_match_issue_evidence(self):
		errors = harness_gate.validate_execution_mode_consistency(
			VALID_ACCEPTANCE,
			VALID_METRICS,
			VALID_PR_BODY.replace("Execution mode: STRICT", "Execution mode: STANDARD"),
		)

		self.assertTrue(any("pull request body" in error for error in errors))


class ChangedPathModeValidationTest(unittest.TestCase):
	def test_solo_rejects_production_and_build_paths(self):
		for path in ("src/main/App.java", "gradle/wrapper/gradle-wrapper.properties", "docker/app.Dockerfile", "build.gradle", "settings.gradle", "gradlew", "gradlew.bat"):
			with self.subTest(path=path):
				errors = harness_gate.validate_changed_path_mode([path], "SOLO")
				self.assertTrue(any("SOLO" in error for error in errors))

	def test_strict_only_paths_reject_non_strict_modes(self):
		for mode in ("SOLO", "STANDARD"):
			with self.subTest(mode=mode):
				errors = harness_gate.validate_changed_path_mode(["scripts/harness_gate.py"], mode)
				self.assertTrue(any("STRICT" in error for error in errors))

	def test_docs_and_gradlew_notes_do_not_trigger_path_mode_rules(self):
		self.assertEqual([], harness_gate.validate_changed_path_mode(["docs/guide.md"], "SOLO"))
		self.assertEqual([], harness_gate.validate_changed_path_mode(["docs/gradlew-notes.md"], "SOLO"))


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
	def test_minimal_role_packet_with_required_references_passes(self):
		packet = {
			"issue_url": "https://github.com/namdongyeob/coffee-order-system/issues/78",
			"worktree_path": "C:/worktrees/issue-78",
			"base_sha": "base",
			"head_sha": "head",
			"acceptance_criteria": "12개 계약 테스트를 반영합니다.",
			"required_documents": [
				"AGENTS.md",
				"docs/ai/orchestration-policy.md",
				"docs/testing/test-strategy.md",
			],
			"diff_scope": "scripts/harness_gate.py와 직접 harness 단위 테스트",
			"previous_p0_p1_finding": "없음",
		}

		self.assertEqual([], harness_gate.validate_role_packet(packet))

	def test_role_packet_rejects_any_non_allowlisted_inline_payload_key(self):
		packet = {
			"issue_url": "https://github.com/namdongyeob/coffee-order-system/issues/78",
			"worktree_path": "C:/worktrees/issue-78",
			"base_sha": "base",
			"head_sha": "head",
			"acceptance_criteria": "본문",
			"required_documents": [
				"AGENTS.md",
				"docs/ai/orchestration-policy.md",
				"docs/testing/test-strategy.md",
			],
			"diff_scope": "scripts/",
			"source_body": "def copied_source(): pass",
			"conversation_history": "전체 대화 로그",
			"prompt": "복사한 source snapshot",
		}

		errors = harness_gate.validate_role_packet(packet)

		self.assertEqual(3, len(errors))
		self.assertIn("source_body", errors[0])
		self.assertIn("conversation_history", errors[1])
		self.assertIn("prompt", errors[2])

	def test_role_packet_requires_three_to_five_canonical_document_paths(self):
		packet = {
			"issue_url": "https://github.com/namdongyeob/coffee-order-system/issues/78",
			"worktree_path": "C:/worktrees/issue-78",
			"base_sha": "base",
			"head_sha": "head",
			"acceptance_criteria": "본문",
			"required_documents": ["AGENTS.md", "docs/ai/orchestration-policy.md"],
			"diff_scope": "scripts/",
		}

		errors = harness_gate.validate_role_packet(packet)

		self.assertEqual(1, len(errors))
		self.assertIn("3~5", errors[0])

		packet["required_documents"] = [
			"AGENTS.md",
			"docs/ai/orchestration-policy.md",
			"docs/testing/test-strategy.md",
			"docs/testing/evidence-guide.md",
			"../not-canonical.md",
		]
		errors = harness_gate.validate_role_packet(packet)

		self.assertEqual(1, len(errors))
		self.assertIn("canonical", errors[0])

	def test_role_packet_rejects_duplicate_or_missing_canonical_documents(self):
		packet = {
			"issue_url": "https://github.com/namdongyeob/coffee-order-system/issues/78",
			"worktree_path": "C:/worktrees/issue-78",
			"base_sha": "base",
			"head_sha": "head",
			"acceptance_criteria": "본문",
			"required_documents": [
				"AGENTS.md",
				"docs/ai/orchestration-policy.md",
				"docs/ai/orchestration-policy.md",
			],
			"diff_scope": "scripts/",
		}

		errors = harness_gate.validate_role_packet(packet)

		self.assertEqual(1, len(errors))
		self.assertIn("distinct", errors[0])

		packet["required_documents"] = [
			"AGENTS.md",
			"docs/ai/orchestration-policy.md",
			"docs/ai/not-a-real-policy.md",
		]
		errors = harness_gate.validate_role_packet(packet)

		self.assertEqual(1, len(errors))
		self.assertIn("existing", errors[0])

	def test_unchanged_repository_after_qa_needs_no_docs_commit_or_second_review(self):
		self.assertEqual(
			{"docs_commit_required": False, "full_review_required": False, "qa_stale": False},
			harness_gate.post_qa_requirements(repository_changed=False, changed_paths=[]),
		)

	def test_runtime_or_policy_change_after_qa_stales_review_and_qa(self):
		for path in ("src/main/java/App.java", "build.gradle", "docs/ai/orchestration-policy.md"):
			with self.subTest(path=path):
				self.assertEqual(
					{"docs_commit_required": False, "full_review_required": True, "qa_stale": True},
					harness_gate.post_qa_requirements(repository_changed=True, changed_paths=[path]),
				)

	def test_github_only_state_update_does_not_require_repository_commit(self):
		self.assertFalse(harness_gate.github_state_requires_repository_commit())

	def test_verification_owners_are_separated(self):
		self.assertEqual("Dev", harness_gate.verification_owner("focused", broad_risk=False))
		self.assertEqual("QA", harness_gate.verification_owner("independent-risk", broad_risk=False))
		self.assertEqual("CI", harness_gate.verification_owner("full-regression", broad_risk=False))

	def test_broad_risk_change_keeps_dev_full_regression(self):
		self.assertEqual("Dev", harness_gate.verification_owner("full-regression", broad_risk=True))

	def test_current_diff_related_failure_cannot_enter_flaky_path(self):
		self.assertEqual(
			"current-issue-defect",
			harness_gate.flaky_next_action(diff_related=True, isolated_result=None, ci_passed=None, blocker_state=None),
		)

	def test_out_of_scope_isolation_pass_and_ci_pass_continue_issue(self):
		self.assertEqual(
			"continue-with-flaky-candidate",
			harness_gate.flaky_next_action(diff_related=False, isolated_result="PASS", ci_passed=True, blocker_state=None),
		)

	def test_out_of_scope_isolation_failure_creates_test_only_blocker(self):
		self.assertEqual(
			"create-test-only-blocker",
			harness_gate.flaky_next_action(diff_related=False, isolated_result="FAIL", ci_passed=None, blocker_state=None),
		)

	def test_unresolved_or_production_blocker_safely_stops(self):
		for blocker_state in ("production-change-required", "cause-unknown", "stabilization-failed"):
			with self.subTest(blocker_state=blocker_state):
				self.assertEqual(
					"blocked-safe-stop",
					harness_gate.flaky_next_action(
						diff_related=False,
						isolated_result="FAIL",
						ci_passed=None,
						blocker_state=blocker_state,
					),
				)

	def test_blocked_without_external_change_does_not_repeat_dispatch_or_verification(self):
		self.assertFalse(harness_gate.blocked_wakeup_requires_work(external_state_changed=False))

	def test_new_pr_can_reach_review_without_future_role_links(self):
		self.assertTrue(
			harness_gate.pre_review_ready(
				dev_verified=True,
				evidence_ready=True,
				pr_body_preflight_passed=True,
			)
		)

	def test_pre_review_requires_each_current_input(self):
		for missing in ("dev_verified", "evidence_ready", "pr_body_preflight_passed"):
			inputs = {
				"dev_verified": True,
				"evidence_ready": True,
				"pr_body_preflight_passed": True,
			}
			inputs[missing] = False
			with self.subTest(missing=missing):
				self.assertFalse(harness_gate.pre_review_ready(**inputs))

	def test_strict_agent_count_uses_unique_roles_only(self):
		roles = ["Dev", "Review", "QA", "Docs", "Main Coordinator", "CI", "Review"]
		self.assertEqual(4, harness_gate.strict_agent_role_count(roles))
		self.assertEqual(3, harness_gate.strict_agent_role_count(["Dev", "Review", "QA", "CI"]))

	def test_missing_evidence_file_fails_lightweight_preflight(self):
		self.assertFalse(harness_gate.required_evidence_exists(["commands.md"]))
		self.assertTrue(
			harness_gate.required_evidence_exists(harness_gate.REQUIRED_EVIDENCE_FILES)
		)

	def test_production_or_test_changes_are_not_docs_metadata(self):
		for path in ("src/main/java/App.java", "src/test/java/AppTest.java"):
			with self.subTest(path=path):
				self.assertFalse(harness_gate.qa_remains_valid("qa", "docs", [path], 71))

	def test_qa_remains_valid_for_issue_evidence_only_delta(self):
		paths = [
			"docs/testing/evidence/issue-71/commands.md",
			"docs/testing/evidence/issue-71/verification.md",
		]
		self.assertTrue(harness_gate.qa_remains_valid("qa-head", "docs-head", paths, 71))

	def test_qa_is_stale_for_non_docs_delta(self):
		paths = ["docs/testing/evidence/issue-71/commands.md", "scripts/harness_gate.py"]
		self.assertFalse(harness_gate.qa_remains_valid("qa-head", "current-head", paths, 71))

	def test_qa_is_current_when_head_did_not_change(self):
		self.assertTrue(harness_gate.qa_remains_valid("same", "same", ["src/main/App.java"], 71))

	def test_other_issue_evidence_does_not_preserve_qa(self):
		self.assertFalse(
			harness_gate.qa_remains_valid(
				"qa", "docs", ["docs/testing/evidence/issue-72/commands.md"], 71
			)
		)

	def test_qa_preservation_rejects_every_path_outside_fixed_markdown_allowlist(self):
		for path in (
			"docs/testing/evidence/issue-71/screenshots/ui.png",
			"docs/testing/evidence/issue-71/capture.png",
			"docs/testing/evidence/issue-71/test-output.txt",
			"docs/testing/evidence/issue-71/arbitrary.md",
			"docs/testing/evidence/issue-72/commands.md",
			"docs/ai/orchestration-policy.md",
			"scripts/harness_gate.py",
			"src/main/java/App.java",
			"src/test/java/AppTest.java",
			"build.gradle",
			".github/workflows/quality-gates.yml",
			"docker/compose.yaml",
		):
			with self.subTest(path=path):
				self.assertFalse(
					harness_gate.qa_remains_valid("qa", "current", [path], 71)
				)

	def test_autonomous_merge_requires_all_existing_gates(self):
		inputs = {
			"review_approved": True,
			"qa_passed": True,
			"docs_evidence_ready": True,
			"ci_passed": True,
			"review_head": "head",
			"current_head": "head",
			"mergeable_clean": True,
		}
		self.assertTrue(harness_gate.autonomous_merge_ready(**inputs))
		for missing in ("review_approved", "qa_passed", "docs_evidence_ready", "ci_passed", "mergeable_clean"):
			case = dict(inputs)
			case[missing] = False
			with self.subTest(missing=missing):
				self.assertFalse(harness_gate.autonomous_merge_ready(**case))

	def test_autonomous_merge_rejects_stale_review_head(self):
		self.assertFalse(
			harness_gate.autonomous_merge_ready(
				review_approved=True,
				qa_passed=True,
				docs_evidence_ready=True,
				ci_passed=True,
				review_head="old",
				current_head="new",
				mergeable_clean=True,
			)
		)

	def test_next_issue_requires_merge_commit_and_closed_issue(self):
		self.assertTrue(harness_gate.next_issue_allowed(True, True, True))
		self.assertFalse(harness_gate.next_issue_allowed(False, True, True))
		self.assertFalse(harness_gate.next_issue_allowed(True, False, True))
		self.assertFalse(harness_gate.next_issue_allowed(True, True, False))

	def test_evidence_guide_pins_lightweight_pr_body_and_preflight_procedure(self):
		repository_root = Path(__file__).resolve().parents[2]
		guide = (repository_root / "docs" / "testing" / "evidence-guide.md").read_text(
			encoding="utf-8"
		)

		for requirement in (
			"Do not restate those values in PR body prose.",
			"observed results, decisions, and remaining risks",
			"Record the Generate start timestamp when the Attempt starts and the Reverification end timestamp when it ends.",
			"do not estimate: record `미측정`",
			"temporary Markdown file outside the repository",
			"python scripts/harness_gate.py --issue <number> --pr-body-file <temporary file>",
			"Use that same passing file",
			"gh pr create --body-file <temporary file>",
			"gh pr edit <PR number> --body-file <temporary file>",
			"Do not use an inline shell body.",
			"Manual QA, Adversarial QA, cleanup receipt, read documents and roles, verification level and result, unverified items, and remaining risks remain required evidence.",
			"The PR body must state material decisions and the remaining risk or gate status",
			"These rules apply to PRs created or edited after Issue #55.",
		):
			with self.subTest(requirement=requirement):
				self.assertIn(requirement, guide)

	def test_skill_keeps_default_coordinator_block_and_policy_merge_exception(self):
		repository_root = Path(__file__).resolve().parents[2]
		skill = (
			repository_root / ".codex" / "skills" / "coffee-order-issue-loop" / "SKILL.md"
		).read_text(encoding="utf-8")

		self.assertIn("BLOCKED: COORDINATOR ONLY", skill)
		self.assertIn(
			"고정 자율 Issue 큐 실험이 활성화되어 있고 모든 정책 merge gate 입력이 확인된 경우에만 Main Coordinator의 merge 또는 Issue close를 예외로 허용합니다.",
			skill,
		)
		self.assertIn(
			"bootstrap Issue #60, 비활성 정책, 승인 큐 밖 Issue, Issue #36 종료 또는 만료, merge gate 입력 하나라도 누락이면 `BLOCKED: COORDINATOR ONLY`를 유지합니다.",
			skill,
		)
		self.assertIn(
			"이 Skill은 GitHub branch protection, ruleset, 기타 설정을 변경하지 않습니다.",
			skill,
		)
		self.assertNotIn("#61 ->", skill)
		self.assertNotIn("required CI checks", skill)

	def test_global_merge_prohibitions_scope_the_fixed_experiment_exception(self):
		repository_root = Path(__file__).resolve().parents[2]
		policy = (repository_root / "docs" / "ai" / "orchestration-policy.md").read_text(
			encoding="utf-8"
		)

		self.assertIn(
			"commit, push, 그리고 고정 자율 Issue 큐 실험 밖에서의 merge, Issue close.",
			policy,
		)
		self.assertIn(
			"고정 자율 Issue 큐 실험 밖에서는 어떤 Agent도 merge 또는 Issue close를 실행하지 않습니다.",
			policy,
		)
		self.assertIn(
			"고정 자율 Issue 큐 실험에서는 아래 열거된 모든 조건을 충족한 Main Coordinator만 merge 또는 Issue close를 실행할 수 있습니다.",
			policy,
		)

	def test_fixed_autonomous_queue_experiment_contract_is_pinned(self):
		repository_root = Path(__file__).resolve().parents[2]
		runbook = (
			repository_root / "docs" / "ai" / "autonomous-queue-runbook.md"
		).read_text(encoding="utf-8")
		agent_rules = (repository_root / "docs" / "ai" / "agent-rules.md").read_text(
			encoding="utf-8"
		)

		runbook_requirements = (
			"`namdongyeob/coffee-order-system`만 적용합니다.",
			"#61 -> #45 -> #55 -> #11 -> #21 -> #12 -> #13 -> #14 -> #15 -> #16 -> #51 -> #52 -> #53 -> #54 -> #56 -> #57 -> #58 -> #36",
			"한 번에 Issue 하나와 production/test 작성자 한 명만 허용합니다.",
			"Issue #60 PR은 자동 merge 또는 close하지 않으며 사람이 직접 merge합니다.",
			"#61은 Issue #60 PR이 사람에 의해 merge된 뒤에만 시작합니다.",
			"#45는 #61이 완료된 뒤에만 시작합니다.",
			"Issue #36이 merge·close되거나 사용자가 중단을 선언하면 즉시 만료됩니다.",
			"최종 팀 프로젝트에는 자동 이전하지 않습니다.",
			"전체 대화를 상속하지 않은 fresh context",
			"`APPROVED`, `REVISE`, `BLOCKED`",
			"원래 Dev에게 한 번만 반환합니다.",
			"fresh Reviewer가 전체 최종 diff를 재검토합니다.",
			"두 번째 `REVISE`이면 자동 루프를 중단하고 사용자에게 보고합니다.",
			"Issue의 측정 가능한 완료 기준을 모두 충족합니다.",
			"필수 Dev verification이 PASS입니다.",
			"fresh Reviewer 최종 판정이 `APPROVED`입니다.",
			"독립 QA가 필요한 검증 Level을 `PASS`로 판정했습니다.",
			"evidence와 실제 역할 보고·명령·수치가 일치하며, Docs Agent를 호출했다면 그 반영도 일치합니다.",
			"required CI checks가 최신 head SHA에서 모두 PASS입니다.",
			"Review가 확인한 head SHA와 merge 직전 head SHA가 같습니다.",
			"PR base가 `main`이고 최신 `origin/main` 기준 merge 가능하며 conflict가 없습니다.",
			"branch protection, required check, review 또는 hook을 우회하지 않습니다.",
			"force push, 관리자 우회, check 무시 merge를 사용하지 않습니다.",
			"중복 Issue를 먼저 검색한 뒤 후속 Issue 후보를 작성합니다.",
			"현재 큐를 막는 P0/P1 결함은 Issue를 생성하고 큐 앞에 삽입할 수 있습니다.",
			"비차단 개선은 backlog Issue로만 생성하고 현재 승인 큐를 자동 확장하지 않습니다.",
			"정책 결정이 필요한 새 Issue는 생성 후 자동 구현하지 않고 사용자에게 보고합니다.",
			"required check·branch protection·GitHub API 상태를 확실히 확인할 수 없음.",
			"프로젝트 정책과 Main Coordinator의 운영 결정이며 GitHub branch protection 또는 ruleset 변경이 아닙니다.",
		)
		for requirement in runbook_requirements:
			with self.subTest(requirement=requirement):
				self.assertIn(requirement, runbook)

		self.assertIn(
			"고정 자율 Issue 큐 실험 밖에서는 사람이 PR merge와 Issue close를 결정합니다.",
			agent_rules,
		)

	def test_core_contract_pins_ci_ground_truth_and_merge_governance(self):
		repository_root = Path(__file__).resolve().parents[2]
		policy = (repository_root / "docs" / "ai" / "orchestration-policy.md").read_text(
			encoding="utf-8"
		)
		runbook = (
			repository_root / "docs" / "ai" / "autonomous-queue-runbook.md"
		).read_text(encoding="utf-8")

		self.assertIn(
			"machine ground truth는 현재 PR head의 GitHub Actions CI run conclusion",
			policy,
		)
		self.assertIn("기본 merge 거버넌스는 사람 도메인 오너의 최종 merge 승인", policy)
		self.assertIn("AI는 1차 결함 탐지와 독립 검증을 담당", policy)
		self.assertIn("`STRICT-lite` 등 새 실행 등급은 추가하지 않습니다", policy)
		# 조건부 문서 분리와 evidence-guide 단일 정본 참조로 도달한 실측 safe floor.
		# 남은 본문은 모드·역할·동시성·거버넌스·동결 등 더 줄일 수 없는 핵심 안전 규칙이다.
		# 회귀 방지용 상한이며, 안전 규칙을 밀어내지 않는 추가 감축이 생기면 더 낮춘다.
		self.assertLessEqual(len(policy.encode("utf-8")), 15360)
		self.assertIn("`namdongyeob/coffee-order-system`만 적용합니다.", runbook)

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
		self.assertRegex(policy, r"`STRICT`.*Dev Agent.*Review Agent.*QA Agent.*CI")
		self.assertNotRegex(policy, r"`STRICT`.*Docs Agent.*CI")
		self.assertIn(
			"Dev가 PR 전 evidence를 완성한 STRICT 흐름에서는 Docs Agent를 기본 dispatch하지 않습니다.",
			policy,
		)
		self.assertIn("metadata 불일치가 있을 때만", policy)
		self.assertIn("실행 모드별 역할 구성은 `docs/ai/orchestration-policy.md`", agent_rules)
		self.assertNotIn("Combined Verifier", agent_rules)

	def test_standard_verifier_timing_and_gate_policy_is_explicit(self):
		repository_root = Path(__file__).resolve().parents[2]
		policy = (repository_root / "docs" / "ai" / "orchestration-policy.md").read_text(
			encoding="utf-8"
		)

		self.assertIn("draft PR을 먼저 생성할 수 있습니다", policy)
		self.assertIn("외부 독립 리뷰", policy)
		self.assertIn("내부 Combined Verifier", policy)
		self.assertIn("독립 Combined Verifier PASS와 CI PASS", policy)
		self.assertIn("draft PR 생성은 완료가 아닙니다", policy)
		self.assertIn("Review Gate와 QA Gate의 판정 기준 자체", policy)

	def test_harness_and_script_hot_path_has_four_required_documents(self):
		repository_root = Path(__file__).resolve().parents[2]
		router = (repository_root / "docs" / "ai" / "context-router.md").read_text(
			encoding="utf-8"
		)
		section = router.split("### 하네스와 스크립트", 1)[1].split("\n### ", 1)[0]

		required_line = next(
			line for line in section.splitlines() if line.startswith("- 필수.")
		)
		self.assertEqual(4, required_line.count("]("))
		self.assertIn("- 조건부.", section)
		self.assertIn("- 제외.", section)
		self.assertIn("- 추가 탐색.", section)

	def test_pull_request_workflow_validates_body_from_temp_file(self):
		repository_root = Path(__file__).resolve().parents[2]
		workflow = (repository_root / ".github" / "workflows" / "harness-quality.yml").read_text(
			encoding="utf-8"
		)

		self.assertIn("PR_BODY: ${{ github.event.pull_request.body }}", workflow)
		self.assertIn("printf '%s' \"$PR_BODY\" > \"$RUNNER_TEMP/pr-body.md\"", workflow)
		self.assertIn('--pr-body-file "$RUNNER_TEMP/pr-body.md"', workflow)

	def test_pull_request_workflow_has_exact_required_event_types(self):
		repository_root = Path(__file__).resolve().parents[2]
		workflow = (repository_root / ".github" / "workflows" / "harness-quality.yml").read_text(
			encoding="utf-8"
		)

		self.assertIn(
			"types: [opened, synchronize, reopened, edited, ready_for_review]", workflow
		)

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
