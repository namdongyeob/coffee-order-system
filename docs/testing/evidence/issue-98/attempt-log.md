# Issue Attempt Log

Issue: #98
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/98
Branch: claude/issue-98-ranking-dedup
Current disposition: PASS
Current Attempt: 1
Current head: 9bacc9314516991af6e84f7689b2c457d831aec2

## Attempt 1

### Generate

`RankingEventProcessor.process()`가 DB 커밋 확정 전에 `PopularMenuRankingService.increment()`(Redis ZINCRBY)를 동기 호출하는 순서 문제를 확인했다. flush 이후 commit 단계에서만 실패하면 DB 행은 롤백되지만 Redis 증가는 되돌아가지 않고, 이후 Kafka 재전송이 오면 같은 주문이 두 번 카운트된다.

수정: `PopularMenuRankingService.increment()`에 `eventId` 파라미터를 추가하고, `popular:menus:processed:{date}` Redis SET(TTL 9일)에 대한 SISMEMBER 확인과 SADD·EXPIRE·ZINCRBY를 하나의 Lua `EVAL`로 원자 실행해 같은 eventId의 두 번째 반영을 무시하도록 했다. `RankingEventProcessor`는 eventId를 그대로 전달하도록 한 줄만 바꿨다. 기존 "Redis 실패 시 DB 롤백" 경로는 건드리지 않았다.

### Evaluate

PASS. 전체 Gradle 테스트 71개(기존 69 + 신규 2) 통과. 신규 테스트 `doesNotDoubleCountWhenIncrementCalledTwiceWithSameEventId`가 같은 eventId로 두 번 호출해도 score가 1.0으로 유지됨을 실제 Redis(Testcontainers)로 검증했다.

### Failure Cause

없음. 다만 첫 전체 회귀 실행에서 `RankingEventConsumerDltIntegrationTest`가 `Container startup failed for image apache/kafka-native:3.9.1`(Wait strategy timeout, exit code 99)로 1건 실패했다. 이 테스트는 이번 diff가 건드리지 않은 파일이라 관련성 확인을 위해 단독 재실행(PASS)과 전체 재실행(69/69 PASS)을 각각 수행해 인프라 일시적 자원 경합(Docker 컨테이너 순차 실행 부하)임을 확인했다. `docs/testing/evidence/issue-77/`(Kafka DLT 통합 테스트 timing flaky 안정화)와 같은 종류의 범위 밖 flaky로 분류하고 재작업하지 않았다.

### Change Scope

`src/main/java/com/example/coffeeordersystem/ranking/service/PopularMenuRankingService.java`, `src/main/java/com/example/coffeeordersystem/ranking/consumer/RankingEventProcessor.java`, 그리고 시그니처 변경을 반영한 테스트 3개 파일(`PopularMenuRankingRedisIntegrationTest.java`, `RankingEventProcessorDatabaseIntegrationTest.java`, `ranking/consumer/RankingEventProcessorTest.java`)만 수정했다.

### Reverification

- `./gradlew.bat compileJava compileTestJava --no-daemon` — BUILD SUCCESSFUL.
- `./gradlew.bat test --no-daemon --tests "com.example.coffeeordersystem.ranking.consumer.RankingEventProcessorTest" --tests "com.example.coffeeordersystem.PopularMenuRankingRedisIntegrationTest" --tests "com.example.coffeeordersystem.RankingEventProcessorDatabaseIntegrationTest" --tests "com.example.coffeeordersystem.RankingEventConsumerKafkaRedisIntegrationTest"` — 12/12 PASS(독립 QA Agent 재실행 기준).
- `./gradlew.bat test --no-daemon`(전체) — 69/69 PASS(독립 QA Agent 재실행 기준; 최초 내 실행에서는 위 flaky 1건 포함 69 tests, 1 failed였고 재실행으로 클린 통과 확인).
- `python -m unittest discover -s scripts/tests -p "test_*.py"` — 160 tests OK(무관 확인용).
- 독립 Review Agent: `APPROVED`, P0/P1/P2 없음.

### Next Attempt

없음.
