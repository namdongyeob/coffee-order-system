# Issue #137 하네스 경량화의 기계 계약을 검증한다.

from __future__ import annotations

import unittest
from pathlib import Path

from scripts import harness_gate


class ImpactClassifierContractTest(unittest.TestCase):
    CASES = (
        (
            "readme",
            (("M", "README.md"),),
            ("SOLO", False, False, False),
        ),
        (
            "current issue evidence only",
            (("M", "docs/testing/evidence/issue-138/verification.md"),),
            ("SOLO", False, False, False),
        ),
        (
            "production source",
            (("M", "src/main/java/com/example/App.java"),),
            ("STANDARD", True, True, True),
        ),
        (
            "test source",
            (("A", "src/test/java/com/example/AppTest.java"),),
            ("STANDARD", True, True, True),
        ),
        (
            "migration",
            (("A", "src/main/resources/db/migration/V2__index.sql"),),
            ("STRICT", True, True, True),
        ),
        (
            "build",
            (("M", "build.gradle"),),
            ("STRICT", True, True, True),
        ),
        (
            "runtime",
            (("M", "docker/compose.yaml"),),
            ("STRICT", True, True, True),
        ),
        (
            "workflow",
            (("M", ".github/workflows/harness-quality.yml"),),
            ("STRICT", False, True, False),
        ),
        (
            "harness",
            (("M", "scripts/harness_gate.py"),),
            ("STRICT", False, True, False),
        ),
        (
            "gate policy",
            (("M", "docs/ai/orchestration-policy.md"),),
            ("STRICT", False, True, False),
        ),
        (
            "api policy",
            (("M", "docs/api/api-spec.md"),),
            ("STANDARD", False, True, False),
        ),
        (
            "domain policy",
            (("M", "docs/domain/order-policy.md"),),
            ("STANDARD", False, True, False),
        ),
        (
            "architecture policy",
            (("M", "docs/architecture/concurrency-strategy.md"),),
            ("STANDARD", False, True, False),
        ),
        (
            "light and source mixed",
            (("M", "README.md"), ("M", "src/main/java/com/example/App.java")),
            ("STRICT", True, True, True),
        ),
        (
            "unknown",
            (("A", "tools/new-runner.toml"),),
            ("STRICT", True, True, True),
        ),
        (
            "rename",
            (("R100", "docs/old.md", "docs/new.md"),),
            ("STRICT", True, True, True),
        ),
        (
            "delete",
            (("D", "README.md"),),
            ("STRICT", True, True, True),
        ),
    )

    def test_single_table_drives_all_four_fail_closed_outputs(self):
        for name, raw_changes, expected in self.CASES:
            with self.subTest(name=name):
                changes = [harness_gate.ChangeRecord(*change) for change in raw_changes]
                impact = harness_gate.classify_change_impact(changes, issue=138)
                self.assertEqual(
                    expected,
                    (
                        impact.execution_mode_floor,
                        impact.requires_java_ci,
                        impact.invalidates_review_qa,
                        impact.invalidates_runtime_evidence,
                    ),
                )

    def test_current_issue_evidence_is_neutral_beside_one_substantive_category(self):
        impact = harness_gate.classify_change_impact(
            [
                harness_gate.ChangeRecord("M", "src/main/java/com/example/App.java"),
                harness_gate.ChangeRecord("M", "docs/testing/evidence/issue-138/verification.md"),
            ],
            issue=138,
        )

        self.assertEqual("STANDARD", impact.execution_mode_floor)
        self.assertTrue(impact.requires_java_ci)

    def test_declared_mode_cannot_lower_the_computed_floor(self):
        impact = harness_gate.classify_change_impact(
            [harness_gate.ChangeRecord("M", "scripts/harness_gate.py")], issue=138
        )

        self.assertNotEqual([], harness_gate.validate_declared_mode_floor("STANDARD", impact))
        self.assertEqual([], harness_gate.validate_declared_mode_floor("STRICT", impact))


class StaleAndMergeContractTest(unittest.TestCase):
    def test_post_qa_helpers_preserve_rename_and_delete_status(self):
        for change in (
            harness_gate.ChangeRecord("R100", "README-new.md", "README.md"),
            harness_gate.ChangeRecord("D", "README.md"),
        ):
            with self.subTest(status=change.status):
                self.assertEqual(
                    {
                        "docs_commit_required": False,
                        "full_review_required": True,
                        "qa_stale": True,
                    },
                    harness_gate.post_qa_requirements(
                        repository_changed=True,
                        changes=[change],
                        issue_number=138,
                    ),
                )
                self.assertFalse(
                    harness_gate.qa_remains_valid(
                        "reviewed-head", "current-head", [change], 138
                    )
                )

    def test_evidence_only_delta_preserves_review_qa_and_runtime_source_evidence(self):
        impact = harness_gate.classify_change_impact(
            [
                harness_gate.ChangeRecord(
                    "M", "docs/testing/evidence/issue-138/verification.md"
                )
            ],
            issue=138,
        )

        self.assertFalse(impact.invalidates_review_qa)
        self.assertTrue(
            harness_gate.runtime_evidence_remains_valid(
                evidence_source_tree_sha="source-sha",
                current_source_tree_sha="source-sha",
                impact=impact,
            )
        )

    def test_source_delta_invalidates_runtime_evidence_linked_to_an_older_source_tree(self):
        impact = harness_gate.classify_change_impact(
            [harness_gate.ChangeRecord("M", "src/test/java/AppTest.java")], issue=138
        )

        self.assertFalse(
            harness_gate.runtime_evidence_remains_valid(
                evidence_source_tree_sha="old-source",
                current_source_tree_sha="new-source",
                impact=impact,
            )
        )

    def test_auto_merge_requires_distinct_final_writer_review_qa_and_source_sha_ci(self):
        inputs = {
            "writer_id": "writer-1",
            "review_id": "review-1",
            "qa_id": "qa-1",
            "review_verdict": "APPROVED",
            "qa_verdict": "PASS",
            "review_head": "source-sha",
            "qa_head": "source-sha",
            "source_tree_head": "source-sha",
            "review_qa_stale": False,
            "docs_evidence_ready": True,
            "ci_passed": True,
            "ci_head": "source-sha",
            "mergeable_clean": True,
        }

        self.assertTrue(harness_gate.autonomous_merge_ready(**inputs))
        for field, blocked_value in (
            ("review_verdict", "REVISE"),
            ("review_verdict", "BLOCKED"),
            ("qa_verdict", "FAIL"),
            ("qa_verdict", "BLOCKED"),
            ("review_qa_stale", True),
            ("ci_passed", False),
        ):
            case = dict(inputs)
            case[field] = blocked_value
            with self.subTest(field=field, value=blocked_value):
                self.assertFalse(harness_gate.autonomous_merge_ready(**case))

        same_agent = dict(inputs, review_id="writer-1")
        missing_agent = dict(inputs, qa_id="")
        stale_qa_head = dict(inputs, qa_head="old-source")
        stale_ci_head = dict(inputs, ci_head="old-source")
        for case in (same_agent, missing_agent, stale_qa_head, stale_ci_head):
            self.assertFalse(harness_gate.autonomous_merge_ready(**case))


class LightweightEvidenceAndRuntimeContractTest(unittest.TestCase):
    ACCEPTANCE_PASS = """# AC
Execution mode: SOLO
Execution mode reason: evidence-only
- [x] done
"""
    ACCEPTANCE_BLOCKED = """# AC
Execution mode: SOLO
Execution mode reason: evidence-only
- [ ] blocked
"""
    VERIFICATION_PASS = """# 검증 로그

Attempt: 2
Head: abcdef1

| 날짜 | Issue | Level | 결과 | 검증 범위 | 명령/Evidence | 비고 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-19 | Issue #138 optional evidence | Level 0 | PASS | gate | command | pass |
"""
    ATTEMPT_BLOCKED = """# Attempt
Issue: #138
Issue URL: https://github.com/example/repo/issues/138
Branch: codex/issue-138-docs
Current disposition: BLOCKED
Current Attempt: 2
Current head: abcdef1

## Attempt 2
### Generate
- change
### Evaluate
- blocked
### Failure Cause
- exact blocker
### Change Scope
- evidence
### Reverification
- partial
### Next Attempt
- resolve blocker
"""

    def test_optional_blocked_attempt_cannot_coexist_with_pass_completion(self):
        errors = harness_gate.validate_evidence_reconciliation(
            self.ACCEPTANCE_PASS,
            self.ATTEMPT_BLOCKED,
            None,
            self.VERIFICATION_PASS,
            138,
        )

        self.assertTrue(any("BLOCKED" in error for error in errors))

    def test_optional_attempt_head_must_match_verification_head(self):
        errors = harness_gate.validate_evidence_reconciliation(
            self.ACCEPTANCE_BLOCKED,
            self.ATTEMPT_BLOCKED.replace("Current head: abcdef1", "Current head: abcdef2"),
            None,
            self.VERIFICATION_PASS.replace("| PASS |", "| PARTIAL |"),
            138,
        )

        self.assertTrue(any("head" in error.lower() for error in errors))

    def test_optional_metrics_retry_count_must_match_attempt(self):
        metrics = """# Metrics
| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| SOLO | 1 | 미측정 | 0 | 0 | 0 | 0 | 0 | 1 |
"""
        errors = harness_gate.validate_evidence_reconciliation(
            self.ACCEPTANCE_BLOCKED,
            self.ATTEMPT_BLOCKED,
            metrics,
            self.VERIFICATION_PASS.replace("| PASS |", "| PARTIAL |"),
            138,
        )

        self.assertTrue(any("retry" in error.lower() for error in errors))

    def test_metrics_and_attempt_details_are_not_default_completion_gates_after_bootstrap(self):
        self.assertEqual(
            ("acceptance-criteria.md",), harness_gate.required_evidence_files(issue=138)
        )

    def test_issue_137_keeps_all_six_preexisting_evidence_files(self):
        self.assertEqual(
            {
                "acceptance-criteria.md",
                "attempt-log.md",
                "commands.md",
                "manual-qa.md",
                "metrics.md",
                "verification.md",
            },
            set(harness_gate.required_evidence_files(issue=137, include_verification=True)),
        )

    def test_active_handle_is_continued_and_same_expensive_input_is_reused(self):
        self.assertEqual(
            "continue-active-handle",
            harness_gate.expensive_command_action(
                active_handle=True,
                completed_same_input=False,
                input_changed=False,
                previous_failed=False,
                flaky_isolation=False,
                classifier_stale=False,
                independent_qa_required=False,
            ),
        )
        self.assertEqual(
            "reuse-completed-result",
            harness_gate.expensive_command_action(
                active_handle=False,
                completed_same_input=True,
                input_changed=False,
                previous_failed=False,
                flaky_isolation=False,
                classifier_stale=False,
                independent_qa_required=False,
            ),
        )

    def test_changed_input_or_independent_qa_allows_one_expensive_run(self):
        for reason in ("input_changed", "previous_failed", "classifier_stale", "independent_qa_required"):
            inputs = {
                "active_handle": False,
                "completed_same_input": True,
                "input_changed": False,
                "previous_failed": False,
                "flaky_isolation": False,
                "classifier_stale": False,
                "independent_qa_required": False,
            }
            inputs[reason] = True
            with self.subTest(reason=reason):
                self.assertEqual("run", harness_gate.expensive_command_action(**inputs))

    def test_wait_contract_allows_only_one_timeout_diagnostic_snapshot(self):
        self.assertEqual(
            "wait-for-notification",
            harness_gate.coordinator_wait_action(
                notification_available=False,
                wait_timed_out=False,
                stall_suspected=False,
                diagnostic_snapshots=0,
            ),
        )
        self.assertEqual(
            "diagnostic-snapshot-once",
            harness_gate.coordinator_wait_action(
                notification_available=False,
                wait_timed_out=True,
                stall_suspected=False,
                diagnostic_snapshots=0,
            ),
        )
        self.assertEqual(
            "wait-for-notification",
            harness_gate.coordinator_wait_action(
                notification_available=False,
                wait_timed_out=True,
                stall_suspected=False,
                diagnostic_snapshots=1,
            ),
        )


class WorkflowContractTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        repository_root = Path(__file__).resolve().parents[2]
        cls.workflow = (repository_root / ".github/workflows/harness-quality.yml").read_text(
            encoding="utf-8"
        )

    def test_required_job_is_stable_while_edited_events_skip_java(self):
        self.assertIn("quality-gates:", self.workflow)
        self.assertIn(
            "github.event.action == 'edited' && 'metadata-gates' || 'quality-gates'",
            self.workflow,
        )
        self.assertIn(
            "github.event.action == 'edited' && 'metadata' || 'source'",
            self.workflow,
        )
        self.assertIn("github.event.action != 'edited'", self.workflow)
        self.assertIn("steps.impact.outputs.requires_java_ci == 'true'", self.workflow)

    def test_workflow_uses_one_harness_link_check_and_one_gradle_invocation(self):
        self.assertEqual(1, self.workflow.count("--check-links"))
        self.assertNotIn("--links-only", self.workflow)
        self.assertEqual(1, self.workflow.count("./gradlew"))
        self.assertIn("./gradlew test --no-daemon", self.workflow)
        self.assertNotIn("compileJava", self.workflow)


if __name__ == "__main__":
    unittest.main()
