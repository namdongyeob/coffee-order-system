# Issue #40 Acceptance Criteria

- [x] `order.completed` listener가 `ranking-consumer-group`으로 이벤트를 소비합니다.
- [x] 새 `eventId`는 `processed_event` insert/flush 후 기존 랭킹 서비스를 한 번 호출하고 DB commit 뒤 정상 반환합니다.
- [x] 정상 완료된 같은 `eventId`의 순차 재전달은 추가 처리 이력이나 랭킹 점수를 만들지 않습니다.
- [x] 서로 다른 `eventId`는 각각 독립 처리합니다.
- [x] Redis 실패 시 `processed_event` insert가 rollback되고 listener 처리가 실패합니다.
- [x] `event_type`, `consumer_group`, `processed_at`을 기존 schema/domain에 기록합니다.
- [x] Redis key/ZSET 구현, migration/unique, retry/error handler/DLT, replay/rebuild/Top3를 변경하지 않습니다.
- [x] Redis 성공 후 DB commit 전 process crash 시 재전달이 score를 중복 증가시킬 수 있는 crash window를 명시합니다.
- [x] 독립 Review가 sentinel 보강 뒤 PASS했으며 최종 수정 필요 항목은 없습니다.
- [x] 독립 QA가 Level 1, 3, 4, 5를 PASS했고 Level 6은 요구하지 않았습니다.

Execution mode: STRICT
Execution mode reason: Kafka Consumer, DB transaction과 Consumer 멱등성 경계를 변경하므로 별도 Dev, Review, QA, Docs 검증과 CI가 필요합니다.
Level 5 required: YES
Level 5 reason: 실제 애플리케이션과 MySQL, Kafka, Redis를 함께 기동하고 listener startup을 확인해야 합니다.
Level 6 required: NO
Level 6 reason: 공개 HTTP API를 추가하거나 변경하지 않는 Kafka Consumer 작업입니다.

## 완료 경계

- 내부 Review와 QA는 PASS했습니다.
- GitHub Actions CI와 사람의 최종 승인 상태는 이 evidence에서 확인하지 않았으므로 pending입니다.
- 보장 범위는 같은 key/partition에서 정상 완료 뒤 순차 재전달되는 Kafka 이벤트입니다. 동시 direct call의 정상 반환, exactly-once, process crash consistency는 보장하지 않습니다.
