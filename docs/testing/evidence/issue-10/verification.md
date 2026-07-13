# 검증 로그

| 날짜 | Issue | Level | 결과 | 검증 범위 | 명령/Evidence | 비고 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-12 | Issue #10 인기 메뉴 Top 3 API | Level 2 | PASS | Controller/API 계약 | `MenuControllerTest`; `docs/testing/evidence/issue-10/commands.md` | Dev가 `GET /api/menus/popular` Controller 계약을 24초에 PASS했습니다. Level 5/6은 required이며 독립 QA가 pending입니다. |
| 2026-07-12 | Issue #10 인기 메뉴 Top 3 API | Level 4 | PASS | Redis ZSET 조회 인프라 통합 | `PopularMenuRedisIntegrationTest`; `docs/testing/evidence/issue-10/commands.md` | Dev가 2 tests, failures 0, errors 0을 55.439초에 PASS했고 독립 QA 재검증도 `BUILD SUCCESSFUL in 1m 26s`였습니다. 최근 7일 ZUNION, `2`/`10` 숫자 동점 정렬, 범위 밖 날짜 제외, 삭제 메뉴 skip, 임시 key 미생성을 확인했습니다. |
| 2026-07-12 | Issue #10 인기 메뉴 Top 3 API | Level 1 | PASS | 빌드·Unit·전체 회귀 smoke | `./gradlew.bat test --no-daemon`; `docs/testing/evidence/issue-10/commands.md` | Dev가 fresh 전체 회귀를 2분 02초에 PASS했습니다. baseline은 ResourceReaper/Docker cleanup 대기와 최종 XML 부재로 PARTIAL/미확인이며 이 PASS 근거와 구분합니다. |
| 2026-07-12 | Issue #10 인기 메뉴 Top 3 API | Level 5 | PASS | 로컬 앱·Redis·Kafka runtime 기동 | `./gradlew.bat bootTestRun --no-daemon`; `docs/testing/evidence/issue-10/manual-qa.md` | 독립 QA populated run에서 MySQL 8.4.5, Kafka 3.9.1, Redis 7.4.2, 앱 56.88초 start와 ranking Consumer partition assigned를 확인했습니다. |
| 2026-07-12 | Issue #10 인기 메뉴 Top 3 API | Level 6 | PASS | 실제 주문·Kafka Consumer·Redis·인기 메뉴 HTTP | `http/issue-10-popular-menu.http`; `docs/testing/evidence/issue-10/manual-qa.md` | prewrite 없이 충전 200, 주문 201 두 건 뒤 processed_event 2와 Redis score 2를 poll로 확인했고 인기 메뉴 API가 HTTP 200과 `[{"rank":1,"menuId":1,"menuName":"아메리카노","orderCount":2}]`를 반환했습니다. |
