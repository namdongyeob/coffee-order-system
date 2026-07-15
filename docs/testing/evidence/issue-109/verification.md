# 검증 로그

Attempt: 1
Head: 6a78ce1f297002b3a0bb6abc86524edb34cf0275

| 날짜 | Issue | Level | 결과 | 검증 범위 | 명령/Evidence | 비고 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-15 | Issue #109 | Level 1 | PASS | `OrderServiceLockTest`의 cleanup 실패·원래 예외 보존·비소유 unlock 금지 | `commands.md` focused lock regression | 8 tests completed. |
| 2026-07-15 | Issue #109 | Level 3 | PASS | 주문·포인트·Outbox 트랜잭션 회귀 | `commands.md` Level 3 | `OrderPaymentIntegrationTest` 5 tests, failures 0. |
| 2026-07-15 | Issue #109 | Level 4 | PASS | 실제 Redis Redisson 락 획득·경합 | `commands.md` Level 4 | `RedisOrderLockIntegrationTest` 1 test, failures 0. |
| 2026-07-15 | Issue #109 | Level 5 | PASS | local 프로필 애플리케이션과 MySQL·Redis·Kafka 기동 | `manual-qa.md` Level 5 local startup | actuator health HTTP 200, status UP. |
| 2026-07-15 | Issue #109 | Level 6 | PASS | 포인트 충전 후 주문 HTTP 성공 | `manual-qa.md` Level 6 point charge and order | charge balance 10000, order HTTP 201 PAID. |
