# Issue Attempt Log

Issue: #10
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/10
Branch: codex/issue-10-popular-menu-api

## Attempt 1 - Popular menu Top 3 API

### Generate

- `GET /api/menus/popular` Controller, response DTO, 최근 7일 인기 메뉴 조회 service와 Redis `ZUNION` 기반 ranking port 구현을 추가했습니다.
- 동점은 `orderCount` 내림차순 뒤 `menuId` 숫자 오름차순으로 애플리케이션에서 정렬합니다.
- DB에서 찾을 수 없는 Redis member는 제외하고 남은 메뉴로 Top 3를 채웁니다.

### Evaluate

- Level 2 Controller test와 Level 4 Redis Testcontainers integration test, fresh Level 1 전체 회귀는 Dev 보고 기준 PASS입니다.
- 초기 RED는 아직 구현되지 않은 `/api/menus/popular`이 404를 반환해 기대한 200과 불일치한 상태였습니다.

### Failure Cause

- TDD RED 시점에는 인기 메뉴 Top 3 API 구현이 없어 `404 Not Found`가 반환됐습니다.

### Change Scope

- 인기 메뉴 조회 API, Redis ranking 조회, 관련 Controller·Redis 통합 테스트, 인기 메뉴 정책 정본과 Issue #10 evidence만 변경합니다.
- Redis ranking write, Kafka Consumer, retry/DLT, replay/rebuild, DB 원천 집계와 QueryDSL 검증 조회는 변경하지 않습니다.

### Reverification

- Level 2 `MenuControllerTest`: PASS, 24초.
- Level 4 `PopularMenuRedisIntegrationTest`: 2 tests, failures 0, errors 0, PASS, 55.439초. 최근 7일 `ZUNION`, 동점 `2`/`10` 숫자 정렬, 범위 밖 날짜 제외, 삭제 메뉴 skip, 임시 key 미생성을 다룹니다.
- Level 1 `./gradlew.bat test --no-daemon`: PASS, 2분 02초.
- Baseline 전체 테스트는 Testcontainers ResourceReaper/Docker cleanup 대기로 종료가 지연됐고 최종 XML을 확인하지 못했습니다. PASS로 재구성하지 않으며 PARTIAL/미확인입니다.

### Next Attempt

- 독립 Review가 diff를 검토합니다. QA의 Level 5·6 결과와 HTTP 원문은 evidence에 반영했습니다.

## Independent QA - Level 5 and Level 6

### Evaluate

- PASS. `./gradlew.bat bootTestRun --no-daemon`으로 MySQL 8.4.5, Kafka 3.9.1, Redis 7.4.2와 애플리케이션을 기동했습니다.
- 애플리케이션은 43.966초에 시작했고 Kafka partition assigned, Redis `PING`의 `PONG`, Redis `DBSIZE`의 `0`을 확인했습니다.
- health와 `GET /api/menus/popular`은 모두 HTTP 200이었고 인기 메뉴 응답 JSON은 `[]`였습니다.

### Failure Cause

- 없음.

### Change Scope

- 코드 변경 없이 독립 runtime·HTTP 검증 결과만 evidence에 기록합니다.

### Reverification

- `curl.exe -sS -i http://localhost:8080/actuator/health` -> HTTP 200, `{"groups":["liveness","readiness"],"status":"UP"}`.
- `curl.exe -sS -i http://localhost:8080/api/menus/popular` -> HTTP 200, `Content-Type: application/json`, `[]`.
- Ctrl+C 뒤 Testcontainers ResourceReaper 지연은 20초 안에 정리됐고 기존 `rag-pgvector`만 남았습니다.

### Next Attempt

- 독립 Review와 GitHub Actions CI를 확인합니다. Level 6에서 populated Top 3 runtime 원문은 수집하지 않았으므로 API의 비어 있지 않은 순위 규칙은 Level 4 integration evidence 범위로만 주장합니다.
