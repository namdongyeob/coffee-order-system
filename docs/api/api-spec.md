# API 명세

Base path는 `/api`입니다. 성공 응답은 envelope 없이 리소스 JSON을 바로 반환하고, 에러만 공통 포맷을 사용합니다.

## GET /api/menus

커피 메뉴 목록을 반환합니다.

```json
[
  { "id": 1, "name": "아메리카노", "price": 4500 }
]
```

- 성공: `200 OK`

## POST /api/points/charge

```json
{ "userId": 1, "amount": 10000 }
```

- `userId`는 양수입니다.
- `amount`는 `1..1,000,000`입니다.
- 지갑이 없으면 생성하고, 있으면 DB 비관적 쓰기 락으로 조회해 충전합니다.

```json
{ "userId": 1, "balance": 10000 }
```

- 성공: `200 OK`

## POST /api/orders

```json
{ "userId": 1, "menuId": 1 }
```

- 두 ID는 모두 양수입니다.
- 메뉴나 사용자 포인트 row가 없거나 잔액이 부족하면 주문을 만들지 않습니다.
- 결제 금액은 요청 값이 아니라 DB의 현재 메뉴 가격입니다.
- 외부 PG 없이 사전 충전 포인트를 차감합니다.
- 같은 사용자의 다중 인스턴스 진입은 `lock:order:user:{userId}` Redisson 락으로 제한합니다. 락 획득은 `waitTime=2s`, `leaseTime=5s`이며 실패하면 `409 ORDER_LOCK_NOT_ACQUIRED`입니다.
- Redisson unlock은 DB 트랜잭션 바깥 cleanup입니다. unlock 확인·실패가 이미 커밋된 주문 성공 응답을 덮지 않습니다.
- 포인트 비관적 락 조회, 차감, `orders`, `outbox_event` 저장은 하나의 DB 트랜잭션입니다. Kafka 발행은 이 요청 트랜잭션에서 직접 수행하지 않습니다.

```json
{
  "orderId": 100,
  "userId": 1,
  "menuId": 1,
  "menuName": "아메리카노",
  "paidAmount": 4500,
  "status": "PAID",
  "orderedAt": "2026-07-18T10:00:00"
}
```

- 성공: `201 Created`

### 주문 뒤 비동기 처리

1. `OutboxEventPublisher`가 2초 주기로 `published_at IS NULL`인 outbox를 Kafka `order.completed`로 발행합니다.
2. `ranking-consumer-group`은 재시도 가능한 실패를 제한 재시도하고, 계속 실패하면 `order.completed.DLT`로 보냅니다.
3. normal consumer와 승인된 DLT 선택 재발행은 `ranking_event_ledger`와 Redis Lua marker를 공유해 같은 eventId를 최대 한 번만 집계합니다.
4. DLT 선택 재발행과 ranking rebuild는 공개 HTTP API가 아니라 명시적으로 활성화하는 운영 경로입니다. recovery lock과 pending rebuild 검사를 통과하지 못하면 fail-closed합니다.

## GET /api/menus/popular

오늘을 포함한 최근 7일 Redis ZSET을 합쳐 주문 수가 많은 메뉴 Top 3를 반환합니다.

```json
[
  {
    "rank": 1,
    "menuId": 1,
    "menuName": "아메리카노",
    "orderCount": 12
  }
]
```

- 성공: `200 OK`
- 정렬: 주문 수 내림차순, 동률이면 메뉴 ID 오름차순입니다.
- Redis는 파생 데이터입니다. Kafka replay로 temp ZSET을 다시 만들고 DB 주문 집계와 비교한 뒤 live ZSET을 교체합니다. 복구는 [Kafka replay 복구 전략](../architecture/recovery-strategy.md)과 [ADR-008 ranking ledger](../adr/ADR-008-ranking-recovery-ledger.md)를 따릅니다.

## 에러 응답

```json
{
  "code": "INSUFFICIENT_POINT",
  "message": "포인트 잔액이 부족합니다."
}
```

| Code | HTTP | 상황 |
| --- | --- | --- |
| `INVALID_CHARGE_AMOUNT` | 400 | 충전액 또는 요청 DTO validation 실패 |
| `MENU_NOT_FOUND` | 404 | 메뉴 없음 |
| `USER_POINT_NOT_FOUND` | 404 | 사용자 포인트 지갑 없음 |
| `INSUFFICIENT_POINT` | 409 | 주문 결제 잔액 부족 |
| `ORDER_LOCK_NOT_ACQUIRED` | 409 | 주문 Redisson 락 획득 실패 또는 대기 중 interrupt |
| `INTERNAL_ERROR` | 500 | 처리되지 않은 서버 오류 |

## 실제 검증

[Issue #114 최종 실행 evidence](../testing/evidence/issue-114/verification.md)에서 메뉴·충전·주문·Top 3의 `200/200/201/200`, 잘못된 충전액 `400`, 잔액 부족 `409`와 주문 뒤 MySQL·Kafka·Redis 일치를 확인했습니다.
