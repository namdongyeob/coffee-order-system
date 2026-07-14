# Issue Attempt Log

Issue: #99
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/99
Branch: claude/issue-99-outbox-pattern
Current disposition: PASS
Current Attempt: 1
Current head: 90ac69680edc620410bfef0c04deb5f76d29b6f5

## Attempt 1

### Generate

`OrderService.createOrder()`가 DB 트랜잭션 커밋 후 `OrderEventPublisher.publish()`를 동기 호출하고 실패는 로그만 남기는 구조를 Transactional Outbox로 교체했다.

- `outbox_event` 테이블(migration V5), `OutboxEvent` 엔티티(`ProcessedEvent`와 동일한 plain JPA 스타일), `OutboxEventRepository`.
- `OrderService.payAndCreateOrder()`가 Order/UserPoint와 같은 트랜잭션에서 `OutboxEvent`를 저장하도록 수정하고, `OrderEventPublisher` 직접 의존을 제거했다.
- `OutboxEventPublisher`(`@Scheduled(fixedDelay=2000ms, 설정 가능)`)가 미발행 이벤트를 폴링해 Kafka로 발행하고, 성공하면 `markPublished` 후 `save()`, 실패(발행 실패·역직렬화 실패)하면 로그만 남기고 다음 폴링에서 재시도한다.
- `@EnableScheduling`을 메인 애플리케이션 클래스에 추가했다.
- `ObjectMapper`가 이 저장소에서 Spring 빈이 아님을 첫 컴파일 실패(`NoSuchBeanDefinitionException`)로 확인하고, `DltReplayService` 관례를 따라 클래스별 `new ObjectMapper().registerModule(new JavaTimeModule())`로 전환했다.

### Evaluate

PASS. 신규 테스트(`OutboxEventPublisherTest` 4개, `OutboxEventIntegrationTest` 2개, `OutboxEventKafkaUnavailableIntegrationTest` 1개)와 기존 관련 테스트(`OrderServiceLockTest`, `OrderPaymentIntegrationTest`, `RedisOrderLockIntegrationTest`, `OrderEventKafkaIntegrationTest`) 전부 PASS. 전체 회귀 76/76.

### Failure Cause

세 가지 중간 실패를 실제로 겪고 원인을 확인한 뒤 수정했다(최종 코드에는 반영되어 있으며, 아래는 그 과정 기록).

1. `ObjectMapper` 빈 부재로 컨텍스트 로딩 실패(`NoSuchBeanDefinitionException`) — DI 제거하고 클래스별 인스턴스 생성으로 전환해 해결.
2. `OutboxEventIntegrationTest`의 `KafkaTestUtils.getSingleRecord`가 "More than one record for topic found"로 실패 — 이 Issue가 추가한 상시 백그라운드 스케줄러 때문에 다른 테스트가 남긴 레코드가 공유 topic에 섞여 있었다. eventId/orderId 기준 필터 폴링으로 교체해 해결.
3. 같은 원인으로 기존 `OrderEventKafkaIntegrationTest`도 동일하게 실패 — 같은 방식(eventId 필터 폴링)으로 수정해 해결.
4. (이 Issue와 무관) 전체 회귀 1회차 도중 Docker Desktop의 WSL 통합이 예기치 않게 중단되어 `RankingEventProcessorDatabaseIntegrationTest`, `RedisOrderLockIntegrationTest`가 컨테이너 기동 실패로 떨어졌다. 사용자가 WSL 통합을 재시작한 뒤 두 테스트 단독 재실행과 전체 재실행 모두 PASS로 인프라 flake임을 확인했다(이 diff와 무관, 코드 변경 없음).

### Change Scope

`CoffeeOrderSystemApplication.java`(`@EnableScheduling` 추가), `OrderService.java`, `application.properties`, `OrderEventKafkaIntegrationTest.java`(공유 topic 필터 폴링), `OrderServiceLockTest.java`(mock 대상 교체) — 기존 파일 5개 수정. `OutboxEvent.java`, `OutboxEventPublisher.java`, `OutboxEventRepository.java`, `V5__create_outbox_event.sql`, `OutboxEventIntegrationTest.java`, `OutboxEventKafkaUnavailableIntegrationTest.java`, `OutboxEventPublisherTest.java` — 신규 파일 7개.

### Reverification

- `./gradlew.bat compileJava compileTestJava --no-daemon` — BUILD SUCCESSFUL.
- focused 7개 클래스(`OutboxEventPublisherTest`, `OrderServiceLockTest`, `OutboxEventIntegrationTest`, `OutboxEventKafkaUnavailableIntegrationTest`, `OrderPaymentIntegrationTest`, `RedisOrderLockIntegrationTest`, `OrderEventKafkaIntegrationTest`) — 17/17 PASS(독립 QA Agent 재실행 기준).
- `./gradlew.bat test --no-daemon`(전체) — 76/76 PASS(Docker WSL 복구 후 재실행 기준).
- `python -m unittest discover -s scripts/tests -p "test_*.py"` — 160 tests OK(무관 확인용, 독립 QA Agent 재실행).
- 독립 Review Agent: `APPROVED`, P0/P1 없음, P2 1건(수평 확장 시 중복 발행 가능성, 이 Issue 범위 밖 후속 과제).

### Next Attempt

없음.
