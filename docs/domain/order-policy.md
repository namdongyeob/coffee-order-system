# 주문 정책

## 트랜잭션

주문 생성과 포인트 차감은 하나의 DB 트랜잭션으로 처리합니다.
이 과제의 주문 결제는 외부 PG/포트원/토스페이먼츠 연동 없이 사전 충전된 포인트 차감으로 처리합니다.

## Issue #6 현재 구현 순서

Issue #6 구현 범위는 DB 트랜잭션과 `UserPoint` 비관적 쓰기 락입니다.
Redisson 진입 락, Kafka 이벤트 발행, Redis 랭킹 반영은 별도 Issue에서 구현합니다.

1. DB 트랜잭션을 시작합니다.
2. `UserPoint`를 비관적 쓰기 락으로 조회합니다.
3. 잔액을 검증합니다.
4. 포인트를 차감합니다.
5. 주문을 저장합니다.
6. 트랜잭션을 커밋합니다.

## 향후 전체 목표 순서

다음 순서는 Redisson, Kafka, Redis 랭킹 Issue까지 완료됐을 때의 목표 흐름입니다.

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
