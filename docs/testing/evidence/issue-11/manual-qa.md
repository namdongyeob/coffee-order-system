# Issue #11 Manual QA

## Level 5 로컬 기동

- Compose MySQL 8.4.5, Redis 7.4.2, Kafka 3.9.1을 모두 healthy로 확인했습니다.
- `local` profile 앱이 Flyway V1~V4를 적용하고 `Started CoffeeOrderSystemApplication in 12.729 seconds`를 기록했습니다.
- `GET /actuator/health`는 HTTP 200과 `{"groups":["liveness","readiness"],"status":"UP"}`를 반환했습니다.

## 실제 DLT 관찰

- Redis만 중지하고 `__TypeId__` header가 있는 정상 `OrderCompletedEvent` JSON을 `order.completed`에 key `6101`로 보냈습니다.
- 앱 로그에서 같은 offset을 대상으로 `Record in retry and not yet recovered`가 2회 기록됐습니다.
- Kafka CLI에서 `order.completed.DLT`의 실제 메시지 한 건을 확인했습니다.
- 원문 핵심 header와 payload는 [dlt-output.txt](dlt-output.txt)에 보존했습니다.
- exception cause는 Redis connection refused였고 original topic은 `order.completed`, consumer group은 `ranking-consumer-group`이었습니다.

## Adversarial QA

- type header 없는 CLI JSON은 listener 전에 역직렬화 실패하므로 이 Issue의 성공 evidence로 사용하지 않았습니다.
- 자동 DLT replay API를 호출하거나 구현하지 않았습니다.

## Cleanup receipt

- 앱을 종료하고 `docker compose -f docker/compose.yaml --profile tools down -v`를 실행했습니다.
- 프로젝트 Compose `ps`는 빈 목록이었습니다.
- `docker ps`에는 기존 `rag-pgvector`만 남았으며 host 3306 MySQL과 다른 프로젝트 리소스를 건드리지 않았습니다.
