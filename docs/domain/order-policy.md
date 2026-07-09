# 주문 정책

## 트랜잭션

주문 생성과 포인트 차감은 하나의 DB 트랜잭션으로 처리합니다.

## 락 처리 순서

1. `lock:order:user:{userId}` 기준 Redisson 락을 획득합니다.
2. DB 트랜잭션을 시작합니다.
3. `UserPoint`를 비관적 쓰기 락으로 조회합니다.
4. 잔액을 검증합니다.
5. 포인트를 차감합니다.
6. 주문을 저장합니다.
7. 트랜잭션을 커밋합니다.
8. `OrderCompletedEvent`를 발행합니다.
9. `finally`에서 Redisson 락을 해제합니다.

## Redisson 기본값

- `waitTime`: 2초.
- `leaseTime`: 5초.
- 락 획득 실패 응답: `409 Conflict`.