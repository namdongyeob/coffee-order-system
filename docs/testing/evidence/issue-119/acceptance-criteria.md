# Issue #119 Acceptance Criteria

Issue: #119
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/119

Execution mode: STRICT
Execution mode reason: MySQL ledger 상태 전이, Redis Lua 원자 적용, Kafka normal consumer·DLT replay·Rebuild recovery lock을 함께 변경합니다.
Level 5 required: YES
Level 5 reason: 실제 Docker Compose의 MySQL·Kafka·Redis와 로컬 consumer·recovery runner 순서에서 양방향 복구와 lock 경합을 관찰해야 합니다.
Level 6 required: NO
Level 6 reason: 외부 HTTP API 계약은 변경하지 않습니다.

## Dev 완료 기준

- [x] 정상 ranking consumer가 `ranking_event_ledger`의 `RESERVED -> REDIS_APPLIED -> COMMITTED` 계약을 사용합니다.
- [x] 같은 eventId·같은 fingerprint는 Redis score를 추가 증가시키지 않습니다.
- [x] 같은 eventId·다른 fingerprint는 Redis와 ledger를 바꾸지 않고 fail-closed 합니다.
- [x] Redis 적용 뒤 DB 상태 전이 실패 후 재시도해도 score가 한 번만 증가합니다.
- [x] `processed_event`는 호환 이력으로 유지하고 DLT 성공 판단의 사전 조회로 사용하지 않습니다.
- [x] pending rebuild run 또는 공용 recovery lock 경합 중 DLT replay는 재발행하지 않고 retryable failure를 반환합니다.
- [x] `DLT -> Rebuild`와 `Rebuild -> DLT` 모두 최종 live ZSET score를 최대 한 번만 반영합니다.
- [x] #112 Rebuild swap 및 bulk backfill 동작, event payload/topic, ranking 정책은 변경하지 않습니다.
- [x] Level 1·3·4·5, 관련 clean과 전체 clean, diff·secret·large-file·preflight를 통과합니다.

## STRICT 후속 게이트

Dev evidence와 Ready PR 뒤 fresh Review, independent QA, 최신 GitHub Actions CI는 Main Coordinator가 확인합니다.
