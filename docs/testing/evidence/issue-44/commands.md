# Issue #44 Commands

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `python -m unittest scripts.tests.test_harness_gate` | TDD RED, 신규 자기 신고·경로·모드·workflow 계약의 누락 확인 | RED. 59 tests 중 14 errors, 2 failures. metrics/path/mode/workflow 계약 부재가 원인입니다. |
| `python -m py_compile scripts/harness_gate.py; python -m unittest scripts.tests.test_harness_gate` | 문법 및 전체 하네스 단위 회귀 | PASS. 59 tests, failures 0, errors 0입니다. |
| `python scripts/harness_gate.py --links-only --base-ref origin/main` | 변경 Markdown 링크 검사 | PASS. `Harness gate PASSED`입니다. |
| `git diff --check` | 공백 오류 정적 검사 | PASS. 오류가 없습니다. |
| `python scripts/harness_gate.py --issue 44 --branch codex/issue-44-harness-self-report-gates --base-ref origin/main --check-links --check-branch --include-worktree --pr-body-file docs/testing/evidence/issue-44/acceptance-criteria.md` | literal `Execution mode: STRICT`와 reason을 가진 임시 PR-body fixture로 3자 모드·branch·evidence·link 검사 | PASS. `Harness gate PASSED`입니다. Fixture는 실제 PR 본문이 아니라 PR-body 형식 필드만 가진 임시 입력으로 사용했습니다. |

CI, 독립 Review, 독립 QA 결과는 이 문서 작성 시점에 pending이며, Dev 결과와 혼동하지 않습니다.
