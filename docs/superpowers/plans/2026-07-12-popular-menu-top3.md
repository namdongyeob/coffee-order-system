# Popular Menu Top 3 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Redis 7.4의 저장 없는 `ZUNION`으로 최근 7일 인기 메뉴 Top 3 API를 제공한다.

**Architecture:** `PopularMenuRankingService`는 일별 ZSET key 7개를 만든 뒤 Redis connection의 `zUnionWithScores`를 호출한다. 서비스 계층은 결과 전체를 score 내림차순·숫자 menuId 오름차순으로 정렬하고, repository에서 존재하는 메뉴만 조합해 세 개까지 반환한다. Controller는 이 결과를 `GET /api/menus/popular`로 노출한다.

**Tech Stack:** Java 17, Spring Boot, Spring Data Redis 4.1, JPA, MockMvc, Testcontainers Redis.

## Global Constraints

- `ZUNIONSTORE`, 임시 Redis key, TTL, 삭제를 사용하지 않는다.
- 오늘을 포함한 정확히 7일의 `popular:menus:{yyyy-MM-dd}` key만 입력으로 사용한다.
- Redis 결과 전체를 읽고 application에서 `orderCount` 내림차순, `menuId` 숫자 오름차순으로 정렬한다.
- 현재 DB에 없는 member는 건너뛰고 뒤 순위 메뉴로 Top 3을 채운다.
- DB 원천 집계, QueryDSL, rebuild/retry/DLT/Kafka는 변경하지 않는다.
- 최종 evidence, verification log, popular-menu policy, HTTP evidence는 Docs 역할만 수정한다.

---

### Task 1: RED controller contract

**Files:**
- Modify: `src/test/java/com/example/coffeeordersystem/menu/controller/MenuControllerTest.java`
- Modify: `src/main/java/com/example/coffeeordersystem/menu/controller/MenuController.java`
- Create: `src/main/java/com/example/coffeeordersystem/menu/dto/PopularMenuResponse.java`

**Interfaces:**
- Produces: `GET /api/menus/popular` → `List<PopularMenuResponse>` with `rank`, `menuId`, `menuName`, `orderCount`.

- [ ] Write a MockMvc test which stubs `MenuService.getPopularMenus()` and asserts HTTP 200 plus the four JSON fields.
- [ ] Run `./gradlew.bat test --tests "*MenuControllerTest" --no-daemon`; expect the missing service/controller API compilation failure.
- [ ] Add the response record, service method contract, and mapped controller method only.
- [ ] Re-run the focused controller test; expect PASS.

### Task 2: RED Redis read and composition behavior

**Files:**
- Create: `src/test/java/com/example/coffeeordersystem/PopularMenuRedisIntegrationTest.java`
- Modify: `src/main/java/com/example/coffeeordersystem/ranking/service/PopularMenuRankingService.java`
- Modify: `src/main/java/com/example/coffeeordersystem/menu/service/MenuService.java`
- Modify: `src/main/java/com/example/coffeeordersystem/menu/repository/MenuRepository.java`

**Interfaces:**
- Consumes: seven date keys and scored Redis members.
- Produces: `PopularMenuRankingService.findRecentSevenDayRankings(LocalDate)` returning scored member values without Redis writes.

- [ ] Write a Testcontainers Redis test seeding seven daily ZSETs and asserting score aggregation, numeric tie ordering (`2` before `10`), a member outside the seven-day window being excluded, a deleted/non-DB member being skipped, and no extra Redis key being created.
- [ ] Run `./gradlew.bat test --tests "*PopularMenuRedisIntegrationTest" --no-daemon`; expect the requested read API compilation failure.
- [ ] Implement only a raw connection `zUnionWithScores` read for the seven keys, map scores/member IDs, sort the full result in Java, resolve valid menus, skip absent ones, then `limit(3)`.
- [ ] Re-run the Redis integration test; expect PASS.

### Task 3: Regression verification and handoff

**Files:**
- Modify only the files above plus this plan; Docs role owns all final evidence files.

- [ ] Run focused Level 2 and Level 4 commands again after the implementation.
- [ ] Run `./gradlew.bat test --no-daemon` for Level 1 full regression.
- [ ] Record command output, RED failure reason, and Level 5/6 runtime outcome in the Dev report for Docs; do not write Docs-owned files.
- [ ] Commit only production, test, DTO, and plan changes with a semantic commit; push `codex/issue-10-popular-menu-api`; create a draft PR whose body contains only observed verification results and `Related: #10`.
