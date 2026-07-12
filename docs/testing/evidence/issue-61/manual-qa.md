# Issue #61 Manual QA

## Level 5

Compose `tools` profile showed all five services running. MySQL, Redis, and Kafka health checks were healthy. A fresh PowerShell local-profile run set `SPRING_PROFILES_ACTIVE=local` and started `CoffeeOrderSystemApplication`; Flyway history contained four rows, Redis replied `PONG`, and Kafka exposed `order.completed`. IntelliJ uses the same `local` active profile and documented `LOCAL_*` variables; the Runbook gives the configuration and run confirmation sequence.

Kafka UI was reachable at `http://localhost:18080` and RedisInsight at `http://localhost:15540`, both with HTTP 200. Kafka UI API returned the `coffee-order-local` cluster with its internal bootstrap server `kafka:9092`, and its topic endpoint returned `order.completed` metadata. RedisInsight API created `coffee-order-local` against Compose-network host `redis:6379` and returned Redis version `7.4.2` with a connection timestamp. Matching raw Redis output confirmed the actual project key `popular:menus:2026-07-12`, member `1`, score `1`.

`git check-ignore -v --no-index .env` resolved to `.gitignore`'s `.env` rule. No `.env` file was created and no credentials were added to Git.

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

## User manual visual QA

The earlier Dev runtime observations and raw API results above do not themselves prove the IntelliJ, Kafka UI, or RedisInsight screens. The following are user-provided visual observations from the corrected Runbook. They are evidence integration only, not fresh independent QA.

- `screenshots/intellij-local-startup.png` captured at 2026-07-12 12:10:18 KST shows the IntelliJ Run console with `Started CoffeeOrderSystemApplication in 13.909 seconds` and the running Kafka consumer.
- `screenshots/kafka-ui-order-completed-topic.png` captured at 2026-07-12 12:07:04 KST shows the Kafka UI `coffee-order-local` context and `order.completed` messages. Two consumed messages have key `6101`.
- `screenshots/redisinsight-popular-menu-key.png` captured at 2026-07-12 12:09:16 KST shows RedisInsight connection `redis:6379` and the `popular:menus:2026-07-12` sorted set with member `1`, score `2`.

The screenshot score `2` is a later user observation after two messages and does not replace the earlier Dev raw observation of score `1`.
