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

## 독립 QA Level 5 and Level 6

- Level 5: `./gradlew.bat bootTestRun --no-daemon` -> MySQL `8.4.5`, Kafka `3.9.1`, Redis `7.4.2` 기동, Redis `PING` `PONG`, `DBSIZE` `0`, application `Started` `43.966s`, Kafka partition assigned, PASS.
- Level 6: `curl.exe -sS -i http://localhost:8080/actuator/health` -> HTTP 200, body `{"groups":["liveness","readiness"],"status":"UP"}`.
- Level 6: `curl.exe -sS -i http://localhost:8080/api/menus/popular` -> HTTP 200, `Content-Type: application/json`, body `[]`.
- Cleanup: Ctrl+C 뒤 Testcontainers ResourceReaper 지연은 20초 안에 정리됐고 기존 `rag-pgvector`만 남았습니다.
- Level 6 제한: runtime Redis `DBSIZE`가 0이어서 populated Top 3 JSON 원문은 관찰하지 못했습니다. populated 순위, 동점, 삭제 메뉴 및 임시 key 미생성은 Level 4 integration test 근거로 분리합니다.
