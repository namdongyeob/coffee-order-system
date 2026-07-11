# Issue #40 Commands

## Dev Attempt 1과 Attempt 2

- Baseline Level 1: `./gradlew.bat test --no-daemon` -> `BUILD SUCCESSFUL in 1m 50s`.
- TDD RED/GREEN: `./gradlew.bat test --tests '*RankingEventProcessorTest' --no-daemon` -> RED는 `RankingEventProcessor` compile failure, GREEN은 4 tests와 `BUILD SUCCESSFUL in 26s`.
- 최초 Level 3: `./gradlew.bat test --tests '*RankingEventProcessorDatabaseIntegrationTest' --no-daemon` -> 3 tests, `BUILD SUCCESSFUL in 1m 19s`.
- 최초 Level 4 RED/GREEN: `./gradlew.bat test --tests '*RankingEventConsumerKafkaRedisIntegrationTest' --no-daemon` -> RED는 `MessageConversionException`, GREEN은 1 test와 `BUILD SUCCESSFUL in 1m 05s`.
- Attempt 2 Level 4: 같은 명령 -> 원본, duplicate, sentinel을 같은 key/partition에 순서대로 발행하고 assertion에서 DB eventId가 원본과 sentinel 2개이며 Redis score가 `2.0`임을 확인, `BUILD SUCCESSFUL in 1m 08s`.
- Attempt 2 Level 3: 같은 focused MySQL 명령 -> 3 tests, `BUILD SUCCESSFUL in 1m 05s`.
- Attempt 2 Level 1: `./gradlew.bat test --no-daemon` -> 43 tests, 0 failures, 0 errors, 0 skipped, `BUILD SUCCESSFUL in 2m 02s`.

## 독립 QA 최종 검증

- Focused unit: `./gradlew.bat test --tests '*RankingEventProcessorTest' --no-daemon` -> 4 tests, `BUILD SUCCESSFUL in 19s`.
- Level 3 actual MySQL: `./gradlew.bat test --tests '*RankingEventProcessorDatabaseIntegrationTest' --no-daemon` -> 순차 duplicate, 다른 eventId, Redis 실패 rollback의 3 tests, `BUILD SUCCESSFUL in 1m 08s`.
- Level 4 actual Kafka/MySQL/Redis: `./gradlew.bat test --tests '*RankingEventConsumerKafkaRedisIntegrationTest' --no-daemon` -> 원본, duplicate, same-key sentinel 순서 검증 1 test, `BUILD SUCCESSFUL in 1m 02s`.
- Level 4 assertion 경계: DB에는 원본과 sentinel eventId만 있어 row count가 2이고 Redis score는 `2.0`입니다. 별도 raw CLI eventId 조회는 수행하지 않았습니다.
- Fresh Level 1: `./gradlew.bat test --no-daemon` -> 43 tests, 0 failures, 0 errors, 0 skipped, `BUILD SUCCESSFUL in 1m 46s`.
- Level 5: `./gradlew.bat bootTestRun --no-daemon` -> `Started CoffeeOrderSystemApplication in 40.173 seconds`, `ranking-consumer-group` partition assigned, health HTTP 200/`UP`.
- Level 5 infra: MySQL `8.4.5`, Kafka `3.9.1`, Redis `7.4.2`가 실행 중임을 확인했습니다.
- Runtime state: Level 6 traffic을 보내지 않았으므로 runtime DB와 Redis ZSET은 비어 있었습니다.
- Level 6: 실행하지 않았습니다. 공개 HTTP API 변경이 없어 요구하지 않습니다.
- Config inspection: retry/error handler/DLT 설정이 없음을 확인했습니다.
- Cleanup: Issue 검증용 애플리케이션과 MySQL/Kafka/Redis를 종료했고 기존 `pgvector`만 남겼습니다.

## Docs Agent 검증

- `git diff --check`.
- `python scripts/harness_gate.py --issue 40 --branch codex/issue-40-kafka-consumer-idempotency --base-ref origin/main --check-links --check-branch --include-worktree`.
- `python -m unittest scripts.tests.test_harness_gate`.

## Timing

- Attempt 1: `882.357s`.
- Attempt 2: `377.941s`.
- Active total: `1260.298s`, 즉 `21.0049667분`, metrics 정수 값은 `21`입니다.
