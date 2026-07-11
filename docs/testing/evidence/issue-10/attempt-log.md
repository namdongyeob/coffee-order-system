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

- 독립 Review와 QA가 diff 및 Level 5·6을 검증합니다. QA는 실제 HTTP 요청·응답 JSON 원문을 `http/issue-10-popular-menu.http`와 manual QA evidence에 채웁니다.
