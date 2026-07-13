# Commands

- `python -m unittest scripts.tests.test_harness_gate.EvidenceValidationTest.test_blocked_current_disposition_rejects_pass_verification scripts.tests.test_harness_gate.EvidenceValidationTest.test_matching_pass_attempt_verification_and_metrics_pass scripts.tests.test_harness_gate.EvidenceValidationTest.test_retry_count_or_current_attempt_or_head_mismatch_fails`
  - 목적: RED 후 세 reconciliation fixture의 GREEN 확인.
  - 결과: 3 tests PASS.
- `python -m unittest discover -s scripts/tests -p "test_*.py"`
  - 목적: harness 전체 unit suite 확인.
  - 결과: 97 tests PASS.
