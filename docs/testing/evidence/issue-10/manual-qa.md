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
