# Issue #10 Commands

## Dev Attempt 1

- TDD RED: `GET /api/menus/popular` 요청은 구현 전 `404 Not Found`를 반환했고, 테스트의 기대 status `200`과 불일치했습니다.
- Level 2: `./gradlew.bat test --tests '*MenuControllerTest' --no-daemon` -> PASS, 24초.
- Level 4: `./gradlew.bat test --tests '*PopularMenuRedisIntegrationTest' --no-daemon` -> 2 tests, failures 0, errors 0, PASS, 55.439초. Redis Testcontainers에서 최근 7일 `ZUNION`, 동점 `2`/`10` 숫자 정렬, 범위 밖 날짜 제외, 삭제 메뉴 skip, 임시 key 미생성을 검증했습니다.
- Level 1: `./gradlew.bat test --no-daemon` -> PASS, 2분 02초.

## Baseline 관찰

- baseline 전체 테스트 실행은 Testcontainers ResourceReaper/Docker cleanup 대기로 종료가 지연됐고 최종 XML 결과를 확인하지 못했습니다.
- 이 baseline은 `PARTIAL`/미확인입니다. 종료 코드, test count, failures를 추정하거나 PASS로 기록하지 않습니다.

## Docs Agent 검증

- `git diff --check` -> PASS.
- `python scripts/harness_gate.py --issue 10 --branch codex/issue-10-popular-menu-api --base-ref origin/main --check-links --check-branch --include-worktree` -> `Harness gate PASSED`.
- `python -m unittest scripts.tests.test_harness_gate` -> 50 tests, PASS.

## Independent QA populated Top 3 follow-up

- `./gradlew.bat test --tests '*PopularMenuRedisIntegrationTest' --no-daemon` -> `BUILD SUCCESSFUL in 1m 26s`.
- `./gradlew.bat bootTestRun --no-daemon` -> MySQL 8.4.5, Kafka 3.9.1, Redis 7.4.2, app `Started` 56.88s, ranking Consumer partition assigned.
- 첫 curl charge command -> PowerShell quoting 오류로 HTTP 400 `INVALID_CHARGE_AMOUNT`. corrected `Invoke-WebRequest` -> HTTP 200, `{"userId":1010,"balance":15000}`. 명령 quoting 오류이며 애플리케이션 실패가 아닙니다.
- `POST /api/orders` request `{"userId":1010,"menuId":1}` twice -> HTTP 201 twice, orderId 1/2, menuName `아메리카노`, paidAmount 4500, status `PAID`.
- Deterministic poll at 06:48:02.356 -> MySQL `processed_event=2`, Redis `ZSCORE popular:menus:2026-07-12 1` -> `2`.
- `GET /actuator/health` -> HTTP 200, `{"groups":["liveness","readiness"],"status":"UP"}`.
- `GET /api/menus/popular` -> HTTP 200, raw `[{"rank":1,"menuId":1,"menuName":"아메리카노","orderCount":2}]`.
- Redis prewrite와 unexpected exception은 없었습니다. Cleanup +5s/+20s에는 pre-existing `rag-pgvector`만 남았습니다.
- Runtime production/test base는 `58bec6911fcd786967b8c54791950e23397186ef`이며 이후 `a7d9477908a4ce7cb26a987224c70cf735ef7406`까지의 advance는 docs-only입니다.

## Fresh GitHub status before final evidence update

- `gh pr view 43 --repo namdongyeob/coffee-order-system --json headRefOid,statusCheckRollup,reviews` -> HEAD `a7d9477908a4ce7cb26a987224c70cf735ef7406`, quality-gates run `29169413405` SUCCESS, GitHub review 없음.
- 이 evidence update 뒤 새 synchronize CI와 내부 Review 재검토는 별도 확인이 필요합니다.

## Final evidence Docs checks

- `git diff --check` -> PASS.
- `python scripts/harness_gate.py --issue 10 --branch codex/issue-10-popular-menu-api --base-ref origin/main --check-links --check-branch --include-worktree` -> `Harness gate PASSED`.
- `python -m unittest scripts.tests.test_harness_gate` -> 50 tests, PASS.

## 독립 QA Level 5 and Level 6

- Level 5: `./gradlew.bat bootTestRun --no-daemon` -> MySQL `8.4.5`, Kafka `3.9.1`, Redis `7.4.2` 기동, Redis `PING` `PONG`, `DBSIZE` `0`, application `Started` `43.966s`, Kafka partition assigned, PASS.
- Level 6: `curl.exe -sS -i http://localhost:8080/actuator/health` -> HTTP 200, body `{"groups":["liveness","readiness"],"status":"UP"}`.
- Level 6: `curl.exe -sS -i http://localhost:8080/api/menus/popular` -> HTTP 200, `Content-Type: application/json`, body `[]`.
- Cleanup: Ctrl+C 뒤 Testcontainers ResourceReaper 지연은 20초 안에 정리됐고 기존 `rag-pgvector`만 남았습니다.
- Level 6 제한: runtime Redis `DBSIZE`가 0이어서 populated Top 3 JSON 원문은 관찰하지 못했습니다. populated 순위, 동점, 삭제 메뉴 및 임시 key 미생성은 Level 4 integration test 근거로 분리합니다.

## CI evidence follow-up

- PR #43 initial `quality-gates` run `29168649292` -> FAILURE. initial PR body의 `STRICT mode:`가 harness required field `Execution mode: STRICT`와 불일치한 것이 유일한 원인입니다.
- live PR body는 `Execution mode: STRICT`와 `Execution mode reason: ...`로 정정됐습니다. `gh pr view 43 --repo namdongyeob/coffee-order-system --json headRefOid,body,statusCheckRollup`에서 HEAD `c41cfee73735cb3188ff5db581a1c25d7ed0aace`가 유지됐고 정정된 live body를 확인했습니다.
- rerun은 기존 pull_request event payload를 사용하므로 stale initial body를 재검사해 실패했습니다. 이 evidence commit push 뒤의 새 synchronize event CI는 pending이며 PASS로 기록하지 않습니다.

## Internal Review FAIL Docs correction

- Review result: FAIL, P1/P2 2건. P1은 Issue 전용 plan의 도달 불가능한 `docs/superpowers/plans/` 위치, P2는 PR #43 재발의 `agent-mistakes.md` 누락입니다.
- Plan move: `git mv docs/superpowers/plans/2026-07-12-popular-menu-top3.md docs/testing/evidence/issue-10/implementation-plan.md`.
- Current CI: HEAD `58bec6911fcd786967b8c54791950e23397186ef`의 quality-gates는 correction 시작 시 pending/in progress이며 PASS로 기록하지 않습니다.
- Populated E2E: 독립 QA가 실행 중이며 결과가 아직 없어 기록하지 않습니다.
- `git diff --check` -> PASS.
- `python scripts/harness_gate.py --issue 10 --branch codex/issue-10-popular-menu-api --base-ref origin/main --check-links --check-branch --include-worktree` -> `Harness gate PASSED`.
- `python -m unittest scripts.tests.test_harness_gate` -> 50 tests, PASS.
