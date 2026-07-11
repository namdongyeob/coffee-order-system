# Issue #44 Commands

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `python -m unittest scripts.tests.test_harness_gate` | TDD RED, 신규 자기 신고·경로·모드·workflow 계약의 누락 확인 | RED. 59 tests 중 14 errors, 2 failures. metrics/path/mode/workflow 계약 부재가 원인입니다. |
| `python -m py_compile scripts/harness_gate.py; python -m unittest scripts.tests.test_harness_gate` | 문법 및 전체 하네스 단위 회귀 | PASS. 59 tests, failures 0, errors 0입니다. |
| `python scripts/harness_gate.py --links-only --base-ref origin/main` | 변경 Markdown 링크 검사 | PASS. `Harness gate PASSED`입니다. |
| `git diff --check` | 공백 오류 정적 검사 | PASS. 오류가 없습니다. |
| `python scripts/harness_gate.py --issue 44 --branch codex/issue-44-harness-self-report-gates --base-ref origin/main --check-links --check-branch --include-worktree --pr-body-file docs/testing/evidence/issue-44/acceptance-criteria.md` | literal `Execution mode: STRICT`와 reason을 가진 임시 PR-body fixture로 3자 모드·branch·evidence·link 검사 | PASS. `Harness gate PASSED`입니다. Fixture는 실제 PR 본문이 아니라 PR-body 형식 필드만 가진 임시 입력으로 사용했습니다. |
| GitHub Actions run `29171462263` | 기준 유효 PR 생성 run | PASS. 2m 40s입니다. |
| GitHub Actions edited run `29171551064` | W2 무효 PR 본문에서 edited trigger와 Harness evidence gate FAIL 확인 | FAILURE. 의도는 `STRICT`→`SOLO`였으나 PowerShell 문자열 갱신이 Markdown 줄바꿈을 평탄화했습니다. CI는 clean SOLO mismatch가 아니라 execution mode 선언과 reason 누락을 보고했습니다. |
| full multiline PR body temp-file preflight, GitHub Actions edited run `29171567906` | W3 유효 본문 복원 후 local preflight와 edited trigger PASS 확인 | PASS. 같은 code HEAD `32e9510`에서 local harness preflight PASS, CI SUCCESS 1m 30s입니다. |
| QA: `python -m py_compile scripts/harness_gate.py` | 하네스 문법 검사 | PASS. |
| QA: `python -m unittest scripts.tests.test_harness_gate` | 전체 하네스 단위 회귀 | PASS. 59 tests, 0.272s입니다. |
| QA: actual Issue gate + valid PR-body fixture | current Issue evidence, diff mode, 3자 execution mode 일치 검사 | PASS. |
| QA: `git diff --check` | 공백 오류 정적 검사 | PASS. |
| QA: negative metrics/path/mode fixtures | 잘못된 metrics·경로·모드 입력이 거부되는지 확인 | expected failures 확인. |
| GitHub Actions run `29171643655` | current code HEAD `ac6afbb` final quality-gates | PASS. 1m 29s입니다. |

W2/W3는 `pull_request.edited` trigger와 invalid-to-valid body FAIL→PASS를 실증합니다. 다만 W2는 malformed body였으므로 clean SOLO mismatch assertion 증거가 아닙니다. final Review는 caveat-aware 재리뷰에서 P0/P1/P2 없음 PASS이고, independent QA도 PASS입니다. 이번 docs evidence push가 생성하는 새 CI run은 pending입니다.
