# Issue #10 Manual QA

## Dev 관찰

- Level 2 Controller 계약은 `MenuControllerTest`로 PASS했습니다.
- Level 4 Redis Testcontainers integration은 최근 7일 합산, 동점 `2`/`10` 숫자 정렬, 범위 밖 날짜 제외, 삭제 메뉴 skip, 임시 key 미생성을 PASS했습니다.

## 독립 QA runtime and HTTP 관찰

- `./gradlew.bat bootTestRun --no-daemon`에서 MySQL 8.4.5, Kafka 3.9.1, Redis 7.4.2를 기동했습니다. 애플리케이션은 43.966초에 시작했고 Kafka partition assigned, Redis `PING` `PONG`, Redis `DBSIZE` `0`을 확인했습니다.
- `curl.exe -sS -i http://localhost:8080/actuator/health`의 관찰 결과는 HTTP 200과 body `{"groups":["liveness","readiness"],"status":"UP"}`입니다.
- `curl.exe -sS -i http://localhost:8080/api/menus/popular`의 관찰 결과는 HTTP 200, `Content-Type: application/json`, body `[]`입니다. 요청·응답 원문 경계는 `http/issue-10-popular-menu.http`에 보관합니다.
- 빈 배열은 runtime Redis `DBSIZE` 0과 일치합니다. 이 관찰은 populated Top 3의 순위·동점·삭제 메뉴 동작을 증명하지 않습니다. 그 범위는 Level 4 Redis Testcontainers integration test로 분리합니다.
- Ctrl+C 뒤 Testcontainers ResourceReaper 지연은 20초 안에 정리됐고 기존 `rag-pgvector`만 남았습니다.

## 독립 QA populated Top 3 end-to-end

- QA는 production/test가 동일한 HEAD `58bec6911fcd786967b8c54791950e23397186ef`에서 실행했습니다. 실행 중 branch가 docs-only commit `a7d9477908a4ce7cb26a987224c70cf735ef7406`까지 전진했지만 production/test diff는 없었습니다.
- `./gradlew.bat test --tests '*PopularMenuRedisIntegrationTest' --no-daemon`은 `BUILD SUCCESSFUL in 1m 26s`였습니다.
- `./gradlew.bat bootTestRun --no-daemon`에서 MySQL 8.4.5, Kafka 3.9.1, Redis 7.4.2가 기동했고 application start는 56.88초, ranking Consumer partition assigned를 확인했습니다.
- 첫 `curl` 충전 시도는 PowerShell quoting 오류로 request body가 잘못 전달돼 HTTP 400 `INVALID_CHARGE_AMOUNT`를 받았습니다. 애플리케이션 결함으로 분류하지 않았고 `Invoke-WebRequest`로 같은 의도의 JSON 요청을 정확히 전달해 성공했습니다.
- 포인트 충전 `{"userId":1010,"amount":15000}`은 HTTP 200과 `{"userId":1010,"balance":15000}`을 반환했습니다.
- 주문 `{"userId":1010,"menuId":1}`을 두 번 요청해 각각 HTTP 201을 받았습니다. 관찰된 응답은 orderId 1과 2, menuName `아메리카노`, paidAmount 4500, status `PAID`입니다.
- Redis를 직접 prewrite하지 않았습니다. 06:48:02.356의 deterministic poll에서 MySQL `processed_event`는 2건이고 Redis `ZSCORE popular:menus:2026-07-12 1`은 `2`였습니다.
- health는 HTTP 200과 `{"groups":["liveness","readiness"],"status":"UP"}`을 반환했습니다.
- 최종 `GET /api/menus/popular`은 HTTP 200과 raw body `[{"rank":1,"menuId":1,"menuName":"아메리카노","orderCount":2}]`를 반환했습니다.
- 예상하지 않은 예외는 없었습니다. cleanup 뒤 +5초와 +20초 확인에서 Issue 검증 컨테이너는 없고 pre-existing `rag-pgvector`만 남았습니다.
