# Issue #9 Commands

## Attempt 1 baseline

- Command: `./gradlew.bat test --no-daemon`
- Purpose: clean `origin/main` 전체 회귀 확인.
- Result: `BUILD FAILED in 1m 50s`, 30 tests, 13 failed. 최초 원인은 일시적 Kafka Testcontainers exit 99였습니다.
- Baseline QA result: focused `BUILD SUCCESSFUL in 1m 09s`, full `BUILD SUCCESSFUL in 1m 16s`, XML 30/0/0/0.

## Attempt 2 prospective timing

- Start timestamp: `2026-07-11T12:21:30.914+09:00`.
- End timestamp command: `$start=[DateTimeOffset]::Parse('2026-07-11T12:21:30.914+09:00'); $end=[DateTimeOffset]::Now; ...`.
- End timestamp result: `2026-07-11T12:29:51.008+09:00`.
- Attempt 2 duration: `500.094s`.
- Combined active Attempt duration: `684.383s`.

## TDD RED

- Command: `./gradlew.bat test --tests "*PopularMenuRankingEntryTest" --tests "*PopularMenuRankingRedisIntegrationTest" --no-daemon`
- Result: `PopularMenuRankingService`가 없어 `compileTestJava`의 `cannot find symbol`로 예상대로 실패, `BUILD FAILED in 18s`.

## GREEN and Level 4 actual Redis

- Command: `./gradlew.bat test --tests "*PopularMenuRankingEntryTest" --tests "*PopularMenuRankingRedisIntegrationTest" --no-daemon`
- Result: unit 2 tests와 실제 Redis Testcontainers integration 3 tests가 통과, `BUILD SUCCESSFUL in 1m 10s`.
- Verified: 같은 날짜·메뉴 2회 score 2, 같은 메뉴의 다른 날짜 key 분리, 같은 날짜의 다른 menu member 분리.

## Level 5 application and Redis runtime

- Start command: `./gradlew.bat bootTestRun --no-daemon`.
- Application result: `Started CoffeeOrderSystemApplication in 41.772 seconds`.
- Health command: `Invoke-WebRequest -Uri 'http://localhost:8080/actuator/health' -UseBasicParsing`.
- Health result: HTTP 200, body status `UP`.
- Redis command: `docker exec d0d37842e623 redis-cli ping`.
- Redis result: Redis 7.4.2 Testcontainers runtime `PONG`.
- Cleanup: `bootTestRun`에 Ctrl+C를 전달해 앱과 Testcontainers를 종료했습니다.

## Repository harness

- Command: `python scripts/harness_gate.py --issue 9 --branch codex/issue-9-redis-ranking-write --base-ref origin/main --check-links --check-branch --include-worktree`.
- Result: `Harness gate PASSED`.

## Level 1 fresh full regression

- Command: `./gradlew.bat test --no-daemon`.
- Result: `BUILD SUCCESSFUL in 1m 19s`.
- XML aggregation command: PowerShell로 `build/test-results/test/TEST-*.xml`의 tests/failures/errors/skipped 합계를 계산했습니다.
- XML result: 35 tests, 0 failures, 0 errors, 0 skipped.

## Commit and pre-push gate

- Commit message: `feat: add daily Redis menu ranking writes`.
- Command: `git push -u origin codex/issue-9-redis-ranking-write`.
- Result: pre-push gate가 `verification-log.md`의 Issue #9 Level 5 PASS 누락을 거부해 push하지 않았습니다.
- Handling: Dev는 Docs Agent 소유 `verification-log.md`를 수정하거나 hook을 우회하지 않습니다.

## Final independent Review

- Result: production 코드 finding 없음. evidence의 Attempt 1과 합산 시간이 각각 1ms 큰 P2 finding 1건으로 `REVISE`.
- Docs correction: `184.289s`, 합계 `684.383s`로 정정했습니다.
- Status: 수정 후 Review 재검토와 승인 여부는 pending입니다.

## Final independent QA

- Level 4 command: `./gradlew.bat test --tests "*PopularMenuRankingEntryTest" --tests "*PopularMenuRankingRedisIntegrationTest" --no-daemon`.
- Level 4 result: 실제 Redis에서 5 tests, 0 failures, 0 errors, 0 skipped, `BUILD SUCCESSFUL in 1m 03s`.
- Level 1 command: `./gradlew.bat test --no-daemon`.
- Level 1 result: 35 tests, 0 failures, 0 errors, 0 skipped, `BUILD SUCCESSFUL in 1m 17s`.
- Level 5 result: MySQL 8.4.5, Kafka 3.9.1, Redis 7.4.2, app start 42.982s, health HTTP 200 `UP`, Redis `PONG`.
- Raw probe: key `popular:menus:2099-12-30`의 member `202` score `1`, member `101` score `2`; key `popular:menus:2099-12-31`의 member `101` score `1`.
- Cleanup: QA 소유 key 2개의 `DEL` 결과 `2`, 후속 `EXISTS` 결과 `0`. 기존 `rag-pgvector`는 변경하지 않았고 QA 작업 리소스는 남지 않았습니다.
- Level 6: 외부 HTTP API 변경이 없어 `NO`.
