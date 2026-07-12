# Issue #66 Commands

| Stage | Command | Result |
| --- | --- | --- |
| RED | `python -m unittest`로 Issue #66의 7개 focused contract test 실행 | Expected failure. `Metadata-only 자동 복구` 정책 절이 없어 7 errors였습니다. |
| GREEN | 같은 7개 focused contract test 실행 | PASS. 7 tests passed. |
| Full | `python -m unittest scripts.tests.test_harness_gate` | PASS. 70 tests passed. |
| Gate | `python scripts/harness_gate.py --issue 66 --branch codex/issue-66-metadata-recovery --base-ref origin/main --check-links --include-worktree` | PASS. |
| Diff | `git diff --check` | PASS. |

## Reverification

- Focused 7 tests, full 70-test harness, repository gate와 diff check가 모두 PASS했습니다.
