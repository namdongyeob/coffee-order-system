# Issue #137 Commands

| Level | 명령 | 목적 | 결과 |
| --- | --- | --- | --- |
| Level 0 | `gh issue view 137 --repo namdongyeob/coffee-order-system --json ...` | 최신 Issue 본문과 bootstrap 경계 확인 | PASS |
| Level 0 | `python -m unittest scripts.tests.test_harness_gate_issue_137` | 새 분류·merge·stale·workflow 계약 RED/GREEN | 구현 전 expected RED, 구현 후 13 tests PASS |
| Level 0 | `python -m unittest discover -s scripts/tests -p "test_*.py"` | 전체 scripts 회귀 | 177 tests PASS |
| Level 0 | `python scripts/harness_gate.py --issue 137 --base-ref origin/main --impact-only --include-worktree` | 실제 diff 네 영향도 출력 확인 | STRICT, Java CI false, Review·QA stale true, runtime stale false |
| Level 0 | `python scripts/harness_gate.py --issue 137 --branch codex/issue-137-harness-lightweight --base-ref origin/main --check-links --check-branch --include-worktree` | evidence·mode·branch·링크 repository gate | PASS |
| Level 0 | `git diff --check` | whitespace 검사 | PASS |

## 미실행

- Gradle, Docker, Testcontainers, Level 3~7은 source/test/build/runtime 입력을 변경하지 않아 실행하지 않았습니다.
