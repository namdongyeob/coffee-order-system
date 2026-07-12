# Issue #66 Commands

| Stage | Command | Result |
| --- | --- | --- |
| RED | `python -m unittest`로 Issue #66의 7개 focused contract test 실행 | Expected failure. `Metadata-only 자동 복구` 정책 절이 없어 7 errors였습니다. |
| GREEN | 같은 7개 focused contract test 실행 | PASS. 7 tests passed. |
| Full | `python -m unittest scripts.tests.test_harness_gate` | PASS. 70 tests passed. |
| Gate | `python scripts/harness_gate.py --issue 66 --branch codex/issue-66-metadata-recovery --base-ref origin/main --check-links --include-worktree` | PASS. |
| Diff | `git diff --check` | PASS. |
| Attempt 2 focused | Issue #66의 정확한 7개 contract test | PASS. 7 tests passed. |
| Attempt 2 full | `python -m unittest scripts.tests.test_harness_gate` | PASS. 70 tests passed. |
| Attempt 2 gate/live body | live PR #67 body를 저장소 밖 임시 파일로 읽은 뒤 `--pr-body-file`을 포함한 Issue #66 repository gate | PASS. |
| Attempt 2 base diff | `git diff --check 2de3a1777ff55df0ac19374a9018d0db58abef86` | PASS before commit with the working-tree correction included. Commit 뒤 `2de3a177...HEAD`로 다시 확인합니다. |
| Attempt 3 RED | pre-review completeness 9개 exact contract tests | Expected failure. 새 정책 절이 없어 9 errors였습니다. |
| Attempt 3 GREEN | 같은 9개 exact contract tests | PASS. 9 tests passed. |
| Attempt 3 full | `python -m unittest scripts.tests.test_harness_gate` | PASS. 79 tests passed. |
| Attempt 3 gate/body/diff | Issue #66 gate, UTF-8 no-BOM 외부 body preflight, base diff check | PASS. 외부 body의 UTF-8 BOM은 `False`였습니다. |

## Reverification

- Focused 7 tests, full 70-test harness, repository gate와 diff check가 모두 PASS했습니다.
- Attempt 3 focused 9 tests, full 79-test harness, gate, body preflight와 base diff check가 PASS했습니다.
