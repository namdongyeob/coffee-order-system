# Issue #137 Commands

| Level | 명령 | 목적 | 결과 |
| --- | --- | --- | --- |
| Level 0 | `gh issue view 137 --repo namdongyeob/coffee-order-system --json ...` | 최신 Issue 본문과 bootstrap 경계 확인 | PASS |
| Level 0 | 네 P1별 단일 `python -m unittest ...` | bootstrap, execution-head status, source gate identity, ready trigger RED/GREEN | 수정 전 expected RED, 수정 후 각 PASS |
| Level 0 | `python -m unittest scripts.tests.test_harness_gate_issue_137` | 새 분류·merge·stale·workflow 계약 | 20 tests PASS |
| Level 0 | `python -m unittest discover -s scripts/tests -p "test_*.py"` | 전체 scripts 회귀 | 184 tests PASS |
| Level 0 | `python scripts/harness_gate.py --issue 137 ... --impact-only --include-worktree` 및 `--issue 138` | bootstrap과 경량화 경계 확인 | #137 Java CI true, #138 Java CI false |
| Level 0 | `python scripts/harness_gate.py --issue 137 --branch codex/issue-137-harness-lightweight --base-ref origin/main --check-links --check-branch --include-worktree` | evidence·mode·branch·링크 repository gate | Attempt 1·2·3 final evidence에서 PASS |
| Level 0 | `git diff --check` | whitespace 검사 | Attempt 1·2·3 PASS |

## 미실행

- 로컬 Gradle은 동일 입력 재실행 금지에 따라 실행하지 않고 새 head의 source `quality-gates`에서 확인합니다. Docker, Testcontainers, Level 2~7은 실행하지 않았습니다.
