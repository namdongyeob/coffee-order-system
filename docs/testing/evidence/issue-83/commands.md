# Commands

- Execution head: `bfaeb36197edb17c4a0543c5d62e00a78fe70b11`
- `python -m unittest scripts.tests.test_harness_gate.EvidenceValidationTest.test_retry_count_mismatch_fails_without_other_metadata_mismatch scripts.tests.test_harness_gate.EvidenceValidationTest.test_current_attempt_must_match_verification_attempt scripts.tests.test_harness_gate.EvidenceValidationTest.test_verification_execution_head_mismatch_fails_without_other_metadata_mismatch scripts.tests.test_harness_gate.EvidenceValidationTest.test_unknown_execution_head_ancestor_fails scripts.tests.test_harness_gate.EvidenceValidationTest.test_code_or_test_change_after_execution_head_fails scripts.tests.test_harness_gate.EvidenceValidationTest.test_evidence_only_delta_after_execution_head_passes`
  - 목적: retry, Attempt, verification head, ancestor, post-head delta, evidence-only delta를 각각 검증.
  - 결과: 6 tests PASS.
- `python -m unittest discover -s scripts/tests -p "test_*.py"`
  - 목적: harness 전체 unit suite 확인.
  - 결과: 103 tests PASS.
