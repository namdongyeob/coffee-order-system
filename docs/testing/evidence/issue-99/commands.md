# Commands

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `find src/main/java -path "*order*"`, `Read OrderService.java`/`OrderEventPublisher.java` | 기존 발행 흐름과 트랜잭션 경계 확인 | DB 커밋 후 동기 발행, 실패는 로그만 남기는 구조를 확인. |
| `Read DltReplayService.java`, `grep -n "kafka"src/main/resources/application.properties` | 기존 Kafka JSON 직렬화·ObjectMapper 관례 확인 | `spring.kafka.producer.value-serializer=JsonSerializer`, `DltReplayService`가 `new ObjectMapper()`를 직접 생성하는 관례를 확인. |
| `Read ProcessedEvent.java` | 신규 엔티티 스타일 참고 | Lombok 없이 plain getter/생성자 스타일을 그대로 따름. |
| `./gradlew.bat compileJava compileTestJava --no-daemon`(1차) | 컴파일 확인 | `NoSuchBeanDefinitionException: ObjectMapper` — 컨텍스트 로딩 실패로 처음 발견. |
| `grep -o "NoSuchBeanDefinitionException..." build/test-results/test/*.xml` | 실패 원인 특정 | ObjectMapper가 Spring 빈이 아님을 확인, DI 제거 후 클래스별 인스턴스 생성으로 수정. |
| `./gradlew.bat test --no-daemon --tests OutboxEventPublisherTest --tests OrderServiceLockTest` | 순수 Mockito 단위 테스트 | PASS. |
| `./gradlew.bat test --no-daemon --tests OutboxEventIntegrationTest ...`(2차) | Testcontainers 통합 테스트 | `IllegalStateException: More than one record for topic found` — 공유 topic에 백그라운드 스케줄러가 남긴 레코드 혼입. |
| `grep -A 3 "<failure" build/test-results/test/*.xml` | 실패 원인 확인 | eventId/orderId 기준 필터 폴링으로 `OutboxEventIntegrationTest`, `OrderEventKafkaIntegrationTest` 수정. |
| `./gradlew.bat test --no-daemon --tests ...`(7개 클래스, 3차) | focused 재검증 | 17/17 PASS. |
| `./gradlew.bat test --no-daemon`(전체, 1차) | 전체 회귀 | Docker Desktop WSL 통합이 테스트 도중 예기치 않게 중단되어 `RankingEventProcessorDatabaseIntegrationTest`, `RedisOrderLockIntegrationTest` 2개 실패(컨테이너 기동 불가). |
| 사용자가 Docker Desktop에서 WSL 통합 재시작, `docker ps`/`docker info`로 복구 확인 | 인프라 복구 | Docker 데몬 정상 응답 확인. |
| `./gradlew.bat test --no-daemon --tests RankingEventProcessorDatabaseIntegrationTest --tests RedisOrderLockIntegrationTest` | flaky 여부 확인 | BUILD SUCCESSFUL(PASS), 인프라 flake로 확인. |
| `./gradlew.bat test --no-daemon`(전체, 2차) | 클린 전체 회귀 재확인 | BUILD SUCCESSFUL, 76 tests, 0 failures(XML 집계 기준). |
| 독립 Review Agent 실행(`git diff main` 직접 확인, 8개 항목 점검) | fresh 독립 코드 리뷰 | `APPROVED`, P0/P1 없음, P2 1건(다중 인스턴스 중복 발행 가능성, 범위 밖). |
| 독립 QA Agent 실행(compile·focused 7클래스·전체·Python 하네스 재실행) | fresh 독립 검증 | `PASS`. focused 17/17, 전체 76/76, Python 하네스 160/160. |
| `git add`(scoped 12개 파일만, `.serena/` 등 무관 파일 제외) 후 `git commit` | 변경 커밋 | `Harness gate PASSED.` 커밋 `90ac69680edc620410bfef0c04deb5f76d29b6f5`. |
