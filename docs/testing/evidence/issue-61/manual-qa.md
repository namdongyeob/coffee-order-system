# Issue #61 Manual QA

## Level 5

Compose `tools` profile showed all five services running. MySQL, Redis, and Kafka health checks were healthy. The app ran with `SPRING_PROFILES_ACTIVE=local`; Flyway history contained four rows, Redis replied `PONG`, and Kafka exposed `order.completed`.

Kafka UI was reachable at `http://localhost:18080` and RedisInsight at `http://localhost:15540`, both with HTTP 200. Kafka UI observes the `coffee-order-local` broker and the `order.completed` topic. RedisInsight can connect to `127.0.0.1:16379` as documented.

## Level 6 raw request and response

```text
GET /actuator/health
HTTP/1.1 200
{"groups":["liveness","readiness"],"status":"UP"}

GET /api/menus
HTTP/1.1 200
[{"id":1,"name":"아메리카노","price":4500},{"id":2,"name":"카페라떼","price":5000},{"id":3,"name":"카푸치노","price":5500},{"id":4,"name":"에스프레소","price":4000}]
```

Actual order observation used user `6101`: point charge returned HTTP 200 with balance `10000`; order creation returned HTTP 201 with order id `1`. Kafka raw record contained `orderId:1`, `userId:6101`, `menuId:1`; Redis contained `popular:menus:2026-07-12` with member `1` score `1`.

The executable request template is `http/issue-61-local-runtime.http`.
