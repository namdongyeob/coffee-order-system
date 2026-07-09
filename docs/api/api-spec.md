# API 명세

Base path는 `/api`입니다.

공통 응답은 과제 구현을 단순하게 유지하기 위해 envelope 없이 리소스 JSON을 바로 반환합니다. 에러만 공통 포맷을 사용합니다.

## GET /api/menus

커피 메뉴 목록을 반환합니다.

### 응답

```json
[
  {
    "id": 1,
    "name": "아메리카노",
    "price": 4500
  }
]
```

## POST /api/points/charge

요청 예시입니다.

```json
{
  "userId": 1,
  "amount": 10000
}
```

### 제약

- `userId`는 양수입니다.
- `amount`는 1 이상입니다.
- 충전 후 잔액은 음수가 될 수 없습니다.

### 응답

```json
{
  "userId": 1,
  "balance": 10000
}
```

## POST /api/orders

요청 예시입니다.

```json
{
  "userId": 1,
  "menuId": 1
}
```

### 처리 기준

- 메뉴가 없으면 주문을 생성하지 않습니다.
- 사용자 포인트 row가 없으면 주문을 생성하지 않습니다.
- 잔액이 부족하면 주문을 생성하지 않습니다.
- 이 과제의 주문 결제는 외부 PG/포트원/토스페이먼츠 연동 없이 사전 충전된 포인트 차감으로 처리합니다.
- Issue #6 범위에서는 `UserPoint` DB 비관적 쓰기 락으로 포인트 차감 정합성을 보호합니다.
- 같은 사용자 주문의 Redisson 진입 락은 별도 Issue에서 추가합니다.
- 주문 저장과 포인트 차감은 하나의 DB 트랜잭션으로 처리합니다.
- 주문 완료 후 `OrderCompletedEvent` Kafka 발행은 별도 Issue에서 추가합니다.

### 응답

```json
{
  "orderId": 100,
  "userId": 1,
  "menuId": 1,
  "menuName": "아메리카노",
  "paidAmount": 4500,
  "status": "PAID",
  "orderedAt": "2026-07-09T10:00:00"
}
```

## GET /api/menus/popular

최근 7일 인기 메뉴 Top 3를 반환합니다.

### 응답

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

### 기준

- 기본 조회 원천은 Redis ZSET입니다.
- Redis 값은 파생 데이터이므로 DB의 주문 원천 데이터와 다를 수 있습니다.
- 랭킹 유실 복구는 `docs/architecture/recovery-strategy.md`를 따릅니다.

## 에러 응답

```json
{
  "code": "INSUFFICIENT_POINT",
  "message": "포인트 잔액이 부족합니다."
}
```

## 초기 에러 코드

| Code | HTTP |
| --- | --- |
| MENU_NOT_FOUND | 404 |
| USER_POINT_NOT_FOUND | 404 |
| INVALID_CHARGE_AMOUNT | 400 |
| INSUFFICIENT_POINT | 409 |
| ORDER_LOCK_FAILED | 409 |
| INTERNAL_ERROR | 500 |

## 상태 코드 기준

| API | 성공 |
| --- | --- |
| GET /api/menus | 200 |
| POST /api/points/charge | 200 |
| POST /api/orders | 201 |
| GET /api/menus/popular | 200 |
