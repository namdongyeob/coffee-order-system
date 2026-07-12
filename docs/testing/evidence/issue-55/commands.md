# Issue #55 Commands

| Stage | Command | Result |
| --- | --- | --- |
| RED | `python -m unittest scripts.tests.test_harness_gate.OrchestrationContractTest.test_evidence_guide_pins_lightweight_pr_body_and_preflight_procedure` | Expected failure. The seven required Evidence Guide statements were absent. |
| GREEN | `python -m unittest scripts.tests.test_harness_gate.OrchestrationContractTest.test_evidence_guide_pins_lightweight_pr_body_and_preflight_procedure` | PASS after the policy text was added. |
| Full | `python -m unittest scripts.tests.test_harness_gate` | PASS. 63 tests passed. |
| Gate | `python scripts/harness_gate.py --issue 55 --branch codex/issue-55-metrics-automation --base-ref origin/main --check-links` | PASS. |
| Diff | `git diff --check` | PASS. |
| Attempt 2 RED | `python -m unittest scripts.tests.test_harness_gate.OrchestrationContractTest.test_evidence_guide_pins_lightweight_pr_body_and_preflight_procedure` | Expected failure. The contract lacked the required-evidence placement and PR decision/gate-status statements. |
| Attempt 2 GREEN | `python -m unittest scripts.tests.test_harness_gate.OrchestrationContractTest.test_evidence_guide_pins_lightweight_pr_body_and_preflight_procedure` | PASS after the missing fixed wording was added. |
| Attempt 2 Full | `python -m unittest scripts.tests.test_harness_gate` | PASS. 63 tests passed. |
| Attempt 2 Gate | `python scripts/harness_gate.py --issue 55 --branch codex/issue-55-metrics-automation --base-ref origin/main --check-links` | PASS. |
| Attempt 2 Live body | repository-external temporary Markdown file with `--pr-body-file` | PASS after the body is updated from the same preflighted file. |
| Attempt 2 Diff | `git diff --check` | PASS. |
