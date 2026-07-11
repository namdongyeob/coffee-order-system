# Issue #40 Commands

## Baseline Level 1

- Command: `./gradlew.bat test --no-daemon`
- Result: `BUILD SUCCESSFUL in 1m 50s`.

## TDD RED and unit GREEN

- Command: `./gradlew.bat test --tests '*RankingEventProcessorTest' --no-daemon`
- RED result: `RankingEventProcessor`의 `cannot find symbol`, `BUILD FAILED in 18s`.
- GREEN result: 4 tests, `BUILD SUCCESSFUL in 26s`.

## Level 3 actual MySQL

- Command: `./gradlew.bat test --tests '*RankingEventProcessorDatabaseIntegrationTest' --no-daemon`
- Result: 신규/duplicate/다른 eventId/Redis 실패 rollback 검증, 3 tests, `BUILD SUCCESSFUL in 1m 19s`.

## Level 4 actual Kafka and Redis

- Command: `./gradlew.bat test --tests '*RankingEventConsumerKafkaRedisIntegrationTest' --no-daemon`
- RED result: 기본 String payload를 `OrderCompletedEvent`로 변환하지 못한 `MessageConversionException`, 1 failed, `BUILD FAILED in 1m 16s`.
- GREEN result: 실제 Kafka 발행, listener 소비, MySQL 처리 이력, Redis score, 같은 eventId duplicate 단일 반영, 1 test, `BUILD SUCCESSFUL in 1m 05s`.

## Level 5 application runtime

- Start command: `./gradlew.bat bootTestRun --no-daemon`.
- Application result: `Started CoffeeOrderSystemApplication in 44.082 seconds`.
- Listener result: `ranking-consumer-group: partitions assigned: [order.completed-0]`.
- Health command: `Invoke-WebRequest -Uri 'http://localhost:8080/actuator/health' -UseBasicParsing`.
- Health result: HTTP 200, status `UP`.
- Infra command: `docker ps --filter 'id=f413079f0099' --filter 'id=6470f83a40f4' --filter 'id=ac6cabf5efc5' --format '{{.ID}} {{.Image}} {{.Status}}'`.
- Infra result: `mysql:8.4.5`, `redis:7.4.2`, `apache/kafka-native:3.9.1` 모두 `Up`.
- Cleanup: `bootTestRun`에 Ctrl+C를 전달해 앱과 Testcontainers를 종료했습니다.

## Fresh Level 1 and harness

- Command: `./gradlew.bat test --no-daemon`.
- Result: `BUILD SUCCESSFUL in 1m 49s`.
- XML result: 43 tests, 0 failures, 0 errors, 0 skipped.
- Command: `python scripts/harness_gate.py --issue 40 --branch codex/issue-40-kafka-consumer-idempotency --base-ref origin/main --check-links --check-branch --include-worktree`.
- Result: `Harness gate PASSED`.
- Command: `git diff --check`.
- Result: whitespace error 없음. `application.properties`의 향후 LF→CRLF 변환 warning만 출력됐습니다.

## Prospective timing

- Start: `2026-07-11T13:54:26.475+09:00`.
- End: `2026-07-11T14:09:08.832+09:00`.
- Exact duration: `882.357s`.
