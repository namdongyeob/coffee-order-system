# Issue #109 Acceptance Criteria

Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/109

Execution mode: STRICT
Execution mode reason: 주문 트랜잭션과 Redisson 분산 락의 성공·실패 경계를 변경하는 동시성 작업입니다.

Level 5 required: YES
Level 5 reason: 실제 Redis 연결 상태에서 애플리케이션이 정상 기동하고 주문 경로가 유지되는지 확인해야 합니다.

Level 6 required: YES
Level 6 reason: 변경 뒤 실제 주문 HTTP 요청이 정상 성공 응답을 반환하는지 확인해야 합니다.

## Acceptance Criteria

- [x] `isHeldByCurrentThread()` 실패가 커밋된 주문 성공 결과를 덮지 않습니다.
- [x] `unlock()` 실패가 커밋된 주문 성공 결과를 덮지 않습니다.
- [x] 주문 처리 중 발생한 원래 비즈니스 예외가 락 정리 예외보다 우선 보존됩니다.
- [x] 락을 획득하지 못했거나 현재 스레드가 소유하지 않으면 `unlock()`을 호출하지 않습니다.
- [x] 정리 실패 경고 로그에 userId, lockKey, 실패 단계가 포함됩니다.
- [x] Level 3/4 focused 검증, Level 5 기동, Level 6 포인트 충전 뒤 주문 HTTP 회귀가 PASS입니다.

## Excluded Scope

- `tryLock(2초, 5초 lease)` 정책 변경.
- 주문 요청 멱등키, Kafka, Redis 랭킹, DLT 변경.
- 공통 락 프레임워크 또는 범용 Manager 추상화.
