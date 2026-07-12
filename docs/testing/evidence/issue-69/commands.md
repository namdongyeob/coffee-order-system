# Issue #69 Commands

| 단계 | 명령 | 결과 |
| --- | --- | --- |
| RED | `python -m unittest scripts.tests.test_harness_gate.OrchestrationContractTest` | 신규 상태 모델 부재로 8 errors, 기대된 실패 |
| Focused GREEN | `python -m unittest scripts.tests.test_harness_gate.OrchestrationContractTest` | 34 tests PASS |
| Full harness | `python -m unittest scripts.tests.test_harness_gate` | 82 tests PASS |
| Repository gate | `python scripts/harness_gate.py --issue 69 --base-ref origin/main --check-links --check-branch --include-worktree` | PASS |
| Static | `git diff --check` | PASS |
| Remediation RED | `python -m unittest scripts.tests.test_harness_gate.OrchestrationContractTest` | snapshot CLI 4 errors와 stale SHA 1 failure, 기대된 실패 |
| Remediation focused | `python -m unittest scripts.tests.test_harness_gate.OrchestrationContractTest` | 39 tests PASS |
| Remediation full harness | `python -m unittest scripts.tests.test_harness_gate` | 87 tests PASS |
| Snapshot CLI E2E | `python scripts/harness_gate.py --issue 69 --queue-state-file scripts/tests/fixtures/queue-state-clean-pr.json --branch codex/issue-69-gate-state-machine` | `PRE_REVIEW_READY`, `DISPATCH_REVIEW_AND_QA`, PASS |
