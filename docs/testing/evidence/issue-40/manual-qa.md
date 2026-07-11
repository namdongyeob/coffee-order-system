# Issue #40 Manual QA

## 확인 결과

- Level 3 실제 MySQL에서 신규 eventId는 처리 이력 1행을 commit하고 정상 완료 duplicate는 추가 행과 랭킹 호출을 만들지 않았습니다.
- 서로 다른 eventId 두 개는 처리 이력 2행과 랭킹 호출 2회로 독립 처리됐습니다.
- Redis 서비스 예외는 listener 처리 경계 밖으로 전파되고 같은 DB transaction의 처리 이력은 rollback됐습니다.
- Level 4 실제 Kafka·Redis에서 같은 이벤트를 두 번 발행해도 `processed_event` 1행과 Redis score `1.0`만 관찰했습니다.
- Level 5에서 애플리케이션, MySQL, Kafka, Redis와 `ranking-consumer-group` listener startup을 확인했습니다.

## Atomicity 한계

- DB transaction은 `processed_event` insert/flush와 Redis 호출 뒤 commit되므로 Redis 실패 시 DB 이력은 rollback됩니다.
- Redis는 DB transaction에 참여하지 않습니다. Redis 성공 후 DB commit 전 process crash가 발생하면 DB 이력이 남지 않아 재전달이 Redis score를 다시 증가시킬 수 있습니다.
- 따라서 이 구현은 정상 완료 후 재전달 멱등성만 제공하며 crash consistency, distributed transaction 또는 exactly-once를 주장하지 않습니다.

## 미검증

- Level 6은 공개 HTTP API 변경이 없어 요구되지 않았습니다.
- retry, error handler, DLT, replay, rebuild는 Issue #40 제외 범위이므로 검증하지 않았습니다.
