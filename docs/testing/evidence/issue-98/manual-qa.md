# Manual QA

이 Issue는 HTTP 계약 변경이 없는 Kafka Consumer 내부 로직 수정이라 Level 5/6(로컬 기동, 실제 HTTP)은 NO입니다. 아래는 실제 Redis(Testcontainers) 상태를 관찰한 결과입니다.

## 관찰 1 — 같은 eventId 재호출 시 중복 미반영

`PopularMenuRankingRedisIntegrationTest.doesNotDoubleCountWhenIncrementCalledTwiceWithSameEventId`

```java
String eventId = UUID.randomUUID().toString();
rankingService.increment(eventId, 1L, orderedAt);
rankingService.increment(eventId, 1L, orderedAt);

assertThat(redisTemplate.opsForZSet().score("popular:menus:2026-07-09", "1"))
        .isEqualTo(1.0);
```

실제 Redis ZSET(`popular:menus:2026-07-09`)의 member `1` score가 두 번 호출 뒤에도 `1.0`으로 유지됨을 확인했습니다(PASS).

## 관찰 2 — 서로 다른 eventId는 각각 반영

같은 파일의 `incrementsSameMenuScoreTwiceOnOrderedDateForDifferentEvents`가 서로 다른 eventId 두 개로 호출하면 score가 `2.0`이 됨을 함께 확인해, "무조건 1로 고정"이 아니라 "같은 주문만 한 번"임을 대조 검증했습니다(PASS).

## 관찰 3 — Kafka+DB+Redis 전체 경로에서 단순 재전송 케이스

`RankingEventConsumerKafkaRedisIntegrationTest.consumesDuplicateBeforeLaterSamePartitionEventWithoutIncrementingTwice`(기존 테스트, 이번 diff로 동작 변경 없음)가 실제 Kafka 발행 → Consumer 소비 → Redis 반영 경로에서 같은 eventId 중복 발행 시 score가 중복 증가하지 않고, 별도 eventId(sentinel)는 정상 반영되어 최종 score `2.0`임을 확인했습니다(PASS, 독립 QA Agent 재실행 기준).

## 미검증 항목

- 실제 DB 커밋이 flush 이후 commit 단계에서 실패하는 시나리오(원 버그의 근본 트리거)는 MySQL Testcontainers로 인위 재현하지 않았습니다. 대신 "increment()가 같은 eventId로 두 번 호출된다"는 그 시나리오의 관찰 가능한 결과를 관찰 1로 직접 재현·검증했습니다.
- Redis 덤프 SET(`popular:menus:processed:{date}`)의 9일 TTL 만료 이후 재계산 동작은 이번 Issue 범위에서 별도로 테스트하지 않았습니다(독립 QA Agent도 동일하게 지적).
