# Issue #40 Acceptance Criteria

- [x] `order.completed` listener가 `ranking-consumer-group`으로 이벤트를 소비합니다.
- [x] 새 `eventId`는 `processed_event` insert/flush 뒤 기존 랭킹 서비스를 1회 호출하고 DB commit 뒤 정상 반환합니다.
- [x] 정상 완료된 같은 `eventId` 재전달은 행과 랭킹 점수를 추가하지 않습니다.
- [x] 서로 다른 `eventId`는 각각 독립 처리합니다.
- [x] Redis 실패 시 `processed_event` insert가 rollback되고 listener 처리는 실패합니다.
- [x] `event_type`, `consumer_group`, `processed_at`을 기존 schema/domain에 기록합니다.
- [x] Redis key/ZSET 구현, migration/unique, retry/error handler/DLT, replay/rebuild/Top3를 변경하지 않습니다.
- [x] Redis 성공 후 DB commit 전 process crash 시 재전달로 score가 중복 증가할 수 있는 crash window를 명시합니다.

Execution mode: STRICT
Execution mode reason: Kafka Consumer, DB transaction과 Consumer 멱등성 경계를 변경하므로 독립 Dev, Review, QA, Docs 검증과 CI가 필요합니다.
Level 5 required: YES
Level 5 reason: 실제 애플리케이션과 MySQL, Kafka, Redis를 함께 기동하고 listener startup을 확인해야 합니다.
Level 6 required: NO
Level 6 reason: 공개 HTTP API를 추가하거나 변경하지 않는 Kafka Consumer 작업입니다.
