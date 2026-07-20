# Issue #141 Commands

- `python -m unittest scripts.tests.test_harness_gate_issue_141` → PASS, 15 tests.
- `python -m unittest discover -s scripts/tests -p "test_*.py"` → PASS, 203 tests, 1.386s.
- `python scripts/harness_gate.py --branch codex/issue-141-coordinator-gates --base-ref origin/main --impact-only --include-worktree` → `execution_mode_floor=STRICT`, `requires_java_ci=false`, `invalidates_review_qa=true`, `invalidates_runtime_evidence=false`.
- `git diff --check` → PASS.

모든 검증은 Python과 Git metadata만 사용했으며 Gradle, Docker, Kafka, DB, 네트워크 명령은 실행하지 않았습니다.
