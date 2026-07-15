# Manual QA

## Level 5 local startup

- `docker/compose.yaml`의 MySQL, Redis, Kafka가 모두 healthy인 상태에서 `local` 프로필 애플리케이션을 기동했습니다.
- `GET http://localhost:8080/actuator/health`는 HTTP 200과 `{"groups":["liveness","readiness"],"status":"UP"}`를 반환했습니다.

## Level 6 point charge and order

- `POST /api/points/charge`에 `{"userId":10901,"amount":10000}`을 전송해 `{"userId":10901,"balance":10000}`을 받았습니다.
- 이어서 `POST /api/orders`에 `{"userId":10901,"menuId":1}`을 전송해 HTTP 201을 받았습니다.
- 주문 응답의 `status`는 `PAID`, `paidAmount`는 4500이었습니다.

## Remaining manual risk

- cleanup 경고 로그의 실제 Redis 장애 발생은 unit mock으로만 재현했습니다. 실제 Redis 락 획득·경합 경로는 Level 4 Testcontainers 검증으로 확인했습니다.
