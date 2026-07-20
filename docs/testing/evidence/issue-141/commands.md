# Issue #141 Commands

- `python -m unittest scripts.tests.test_team_orchestration scripts.tests.test_harness_gate_issue_141` → PASS, 61 tests, 1.732s.
- `python -m unittest discover -s scripts/tests -p "test_*.py"` → PASS, 219 tests, 5.333s.
- `python scripts/harness_gate.py --issue 141 --branch codex/issue-141-coordinator-gates --base-ref origin/main --impact-only --include-worktree` → `execution_mode_floor=STRICT`, `requires_java_ci=false`, `invalidates_review_qa=true`, `invalidates_runtime_evidence=false`.
- `python scripts/harness_gate.py --issue 141 --branch codex/issue-141-coordinator-gates --base-ref origin/main --check-links --check-branch --include-worktree` → `Harness gate PASSED.`
- `git diff --check` → PASS.

모든 검증은 Python과 Git metadata만 사용했으며 Gradle, Docker, Kafka, DB, 네트워크 명령은 실행하지 않았습니다.
