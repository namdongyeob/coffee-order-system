# Commands

## TDD red

- Command: `.\gradlew.bat test --tests com.example.coffeeordersystem.order.service.OrderServiceLockTest --no-daemon`
- Result: FAIL, 8 tests 중 새 실패 경로 4건 실패. 기존 `finally`의 `isHeldByCurrentThread()` 또는 `unlock()` 예외가 결과를 덮는 것을 재현했습니다.

## Focused lock regression

- Command: `.\gradlew.bat test --tests com.example.coffeeordersystem.order.service.OrderServiceLockTest --no-daemon`
- Result: PASS, BUILD SUCCESSFUL. 8 tests completed.

## Level 3

- Command: `.\gradlew.bat test --tests com.example.coffeeordersystem.OrderPaymentIntegrationTest --no-daemon`
- Result: PASS. Testcontainers MySQL·Redis·Kafka 환경에서 5 tests, failures 0, errors 0.

## Level 4

- Command: `.\gradlew.bat test --tests com.example.coffeeordersystem.RedisOrderLockIntegrationTest --no-daemon`
- Result: PASS. 실제 Redis Redisson 락 경합 검증 1 test, failures 0, errors 0.

## Level 5 and 6

- Command: `docker compose -f docker\compose.yaml up -d --wait`
- Result: PASS. MySQL, Redis, Kafka가 모두 healthy.
- Command: `bootRun --args=--spring.profiles.active=local` 후 `GET /actuator/health`
- Result: PASS. HTTP 200, `status`는 `UP`.
- Command: `POST /api/points/charge` userId 10901 amount 10000 후 `POST /api/orders` userId 10901 menuId 1
- Result: PASS. 충전 응답 balance 10000, 주문 HTTP 201, status `PAID`, paidAmount 4500.
