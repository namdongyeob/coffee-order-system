# Issue #61 Acceptance Criteria

Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/61

Execution mode: STRICT
Execution mode reason: MySQL, Redis, Kafka runtime configuration and Level 5/6 local execution contract change together.
Level 5 required: YES
Level 5 reason: A new PowerShell session and IntelliJ local profile must start the real application against Compose infrastructure.
Level 6 required: YES
Level 6 reason: Health and the representative menu API must return real HTTP responses.

- [x] Compose provides MySQL 13306, Redis 16379, Kafka 19092 without touching a host MySQL on 3306.
- [x] `tools` profile starts Kafka UI and RedisInsight.
- [x] local profile applies four Flyway migrations and connects to MySQL, Redis, and Kafka.
- [x] health and `GET /api/menus` return HTTP 200.
- [x] Kafka topic and Redis ranking key are observed after a real order.
- [ ] Fresh Review, independent QA, Docs final synchronization, and CI remain coordinator gates.
