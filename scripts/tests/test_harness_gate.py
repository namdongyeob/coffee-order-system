# 하네스 품질 게이트의 성공과 실패 조건을 검증하는 테스트
import tempfile
import unittest
from pathlib import Path

from scripts import harness_gate


VALID_ACCEPTANCE = """# Issue #23 Acceptance Criteria

Level 5 required: NO
Level 5 reason: 애플리케이션 동작을 변경하지 않는 저장소 운영 작업입니다.
Level 6 required: NO
Level 6 reason: 호출할 API가 없는 저장소 운영 작업입니다.
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

	def test_attempt_log_sections_are_required(self):
		errors = harness_gate.validate_attempt_log("## Attempt 1\n### Generate\n")
		self.assertTrue(any("### Evaluate" in error for error in errors))
		self.assertTrue(any("### Next Attempt" in error for error in errors))

	def test_attempt_log_issue_linkage_is_required(self):
		content = VALID_ATTEMPT.replace("Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/23\n", "")
		errors = harness_gate.validate_attempt_log(content)
		self.assertTrue(any("Issue URL" in error for error in errors))

	def test_verification_log_must_reference_issue(self):
		errors = harness_gate.validate_verification_log("| Issue #22 | PASS |", 23)
		self.assertEqual(["verification-log.md에 Issue #23 기록이 없습니다."], errors)

	def test_verification_log_requires_result_row(self):
		errors = harness_gate.validate_verification_log("Issue #23", 23)
		self.assertEqual(["verification-log.md에 Issue #23 결과 행이 없습니다."], errors)

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
			verification.write_text("| 2026-07-10 | Issue #23 | PASS |", encoding="utf-8")

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
			verification.write_text("| 2026-07-10 | Issue #23 | PASS |", encoding="utf-8")

			self.assertEqual([], harness_gate.validate_issue_evidence(root, 23))


class MarkdownLinkTest(unittest.TestCase):
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
	def test_main_is_coordinator_only_and_qa_verifies(self):
		repository_root = Path(__file__).resolve().parents[2]
		skill = (
			repository_root / ".codex" / "skills" / "coffee-order-issue-loop" / "SKILL.md"
		).read_text(encoding="utf-8")

		self.assertIn("BLOCKED: COORDINATOR ONLY", skill)
		self.assertIn("Review와 QA를 병렬 배정", skill)
		self.assertIn("독립 Issue는 별도 worktree", skill)
		self.assertIn("BLOCKED: AGENT STALLED", skill)
		self.assertIn("commit, push, merge", skill)
		self.assertIn("docs/testing/test-strategy.md", skill)
		self.assertNotIn("BLOCKED: MAIN VERIFIES", skill)

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
