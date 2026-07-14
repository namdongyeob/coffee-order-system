# Manual QA

이 Issue는 HTTP 계약 변경이 없는 내부 이벤트 발행 신뢰성 개선이라 Level 5/6(로컬 기동, 실제 HTTP)은 NO입니다. 아래는 실제 DB(MySQL Testcontainers)·Kafka(Testcontainers) 상태를 관찰한 결과입니다.

## 관찰 1 — Kafka가 항상 실패해도 주문은 성공하고 OutboxEvent가 남는다

`OutboxEventKafkaUnavailableIntegrationTest.createOrderCommitsAndKeepsOutboxEventPendingWhenKafkaAlwaysFails`

```java
when(orderEventPublisher.publish(any()))
        .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("kafka unavailable")));

OrderResponse response = orderService.createOrder(301L, 1L);

assertThat(response.status().name()).isEqualTo("PAID");
assertThat(userPointRepository.findByUserId(301L)).get()
        .extracting(UserPoint::getBalance).isEqualTo(5_500);
OutboxEvent outboxEvent = outboxEventRepository.findAll().get(0);
assertThat(outboxEvent.getPublishedAt()).isNull();
```

주문이 정상 성공(PAID)하고 포인트가 정확히 차감되며, `OutboxEvent` 행이 `published_at = null`로 실제 DB에 남음을 확인했습니다(PASS). 기존 구조였다면 이 시나리오에서 이벤트가 영구 유실됐을 것입니다.

## 관찰 2 — 실제 Kafka로 전달되고 발행 상태가 갱신된다

`OutboxEventIntegrationTest.publishPendingDeliversOutboxEventToKafkaAndMarksItPublished`가 실제 Kafka consumer로 `order.completed` topic을 구독한 상태에서 `outboxEventPublisher.publishPending()`을 직접 호출해, (1) `OutboxEvent.published_at`이 non-null로 갱신되고, (2) 실제 Kafka 레코드에 해당 orderId·userId가 담겨 도착함을 함께 확인했습니다(PASS).

## 관찰 3 — 일시적 실패 후 재시도로 결국 발행된다

`OutboxEventPublisherTest.publishesOnSubsequentPollAfterTransientFailure`가 1차 폴링 실패 → `published_at` null 유지 → 2차 폴링 성공 → `published_at` non-null로 갱신되는 흐름을 Mockito 체이닝(`thenReturn(failed).thenReturn(success)`)으로 직접 관찰했습니다(PASS).

## 미검증 항목

- 다중 애플리케이션 인스턴스가 동시에 `publishPending()`을 폴링할 때의 claim/lock 부재(같은 행을 두 인스턴스가 동시에 집어 중복 발행할 가능성)는 이번 범위(단일 인스턴스 MVP)에서 테스트하지 않았습니다. 독립 Review Agent가 P2로 지적했고, 수평 확장 시 후속 과제로 남깁니다.
- `outbox_event` 테이블의 장기 보관·정리(archiving) 정책은 이번 Issue 범위 밖입니다.
