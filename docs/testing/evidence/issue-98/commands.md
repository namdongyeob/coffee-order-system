# Commands

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `find src/main/resources/db -iname "*.sql"`, `grep -rn "expire\|TTL" src/main/java/.../ranking/` | Redis 키 TTL·Lua 스크립트 기존 관례 확인(RankingRebuildLock) | 기존 저장소가 `DefaultRedisScript` + `StringRedisTemplate.execute(script, keys, args...)` 패턴을 이미 사용 중임을 확인, 동일 패턴으로 구현. |
| `./gradlew.bat compileJava compileTestJava --no-daemon` | 컴파일 확인 | BUILD SUCCESSFUL. `PopularMenuRankingService.java`의 deprecated API 경고는 변경 전부터 존재(`git stash`로 확인). |
| `./gradlew.bat test --no-daemon --tests "com.example.coffeeordersystem.ranking.consumer.RankingEventProcessorTest"` | Mockito 기반 단위 테스트 | PASS. |
| `docker ps` | Testcontainers 실행 전 Docker 가용성 확인 | 실행 중 확인. |
| `./gradlew.bat test --no-daemon --tests "...PopularMenuRankingRedisIntegrationTest" --tests "...RankingEventProcessorDatabaseIntegrationTest" --tests "...RankingEventConsumerKafkaRedisIntegrationTest"` | Redis/DB/Kafka+Redis 통합 테스트(Level 4) | BUILD SUCCESSFUL. |
| `./gradlew.bat test --no-daemon`(전체, 1차) | 전체 회귀(Kafka 공통 consumer 변경이라 push 전 전체 실행) | 69 tests, 1 failed(`RankingEventConsumerDltIntegrationTest`, Kafka Testcontainers 기동 timeout). |
| `grep -rl "<failure" build/test-results/test/*.xml`, XML 원인 확인 | 실패가 이번 diff와 관련 있는지 확인 | 원인은 `Container startup failed for image apache/kafka-native:3.9.1`(`Timed out waiting for log output matching '.*Transitioning from RECOVERY to RUNNING.*'`). 해당 테스트는 이번 diff가 건드리지 않은 파일. |
| `./gradlew.bat test --no-daemon --tests "...RankingEventConsumerDltIntegrationTest"`(단독 재실행) | flaky 여부 확인 | BUILD SUCCESSFUL(PASS). |
| `./gradlew.bat test --no-daemon`(전체, 2차) | 클린 전체 회귀 재확인 | BUILD SUCCESSFUL(69/69 PASS). |
| 독립 Review Agent 실행(`git diff main` 직접 확인) | fresh 독립 코드 리뷰 | `APPROVED`, P0/P1/P2 없음. |
| 독립 QA Agent 실행(compile·focused·전체·Python 하네스 재실행) | fresh 독립 검증 | `PASS`. focused 12/12, 전체 69/69, Python 하네스 160/160. 신규 테스트가 fix 없이는 실패했을 것임을 인접 테스트(다른 eventId 케이스)와 대조해 확인. |
| `git add -- src/main/... src/test/...` 후 `git commit` | 변경 커밋 | `Harness gate PASSED.` 커밋 `9bacc9314516991af6e84f7689b2c457d831aec2`. |
