# Kafka 이벤트 흐름

## Topic

- 메인 topic: `order.completed`.
- DLT topic: `order.completed.DLT`.

## Event

```json
{
  "eventId": "uuid",
  "orderId": 1,
  "userId": 1,
  "menuId": 1,
  "paidAmount": 4500,
  "orderedAt": "2026-07-09T12:00:00"
}
```

## Producer 규칙

- 성공한 DB 트랜잭션 이후 발행합니다.
- 메시지 key 추천값은 `userId`입니다.

## Consumer

- `ranking-consumer-group`은 Redis ZSET 랭킹을 갱신합니다.
- `ranking-rebuild-group`은 Redis 랭킹 재구성 도전 기능에 사용합니다.

## 에러 처리

- `DefaultErrorHandler`를 사용합니다.
- `FixedBackOff(1000L, 2L)`로 재시도합니다.
- `DeadLetterPublishingRecoverer`로 실패 메시지를 DLT로 이동합니다.