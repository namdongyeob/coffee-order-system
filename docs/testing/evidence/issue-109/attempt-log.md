# Issue Attempt Log

Issue: #109
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/109
Branch: issue-109
Current disposition: PASS
Current Attempt: 1
Current head: 6a78ce1f297002b3a0bb6abc86524edb34cf0275

## Attempt 1

### Generate

- `OrderService`의 락 정리를 `releaseLockSafely`로 분리했습니다.
- 소유 확인과 unlock 예외를 각각 격리하고 userId, lockKey, 단계가 포함된 경고 로그를 추가했습니다.
- 실패 경로와 정상 주문 회귀 focused 테스트를 추가했습니다.

### Evaluate

- PASS. TDD 실패 단계에서 새 테스트 4건이 기존 finally 예외 전파로 실패했고, 최소 구현 뒤 focused 및 실제 인프라 검증이 통과했습니다.

### Failure Cause

- 없음.

### Change Scope

- `src/main/java/com/example/coffeeordersystem/order/service/OrderService.java`
- `src/test/java/com/example/coffeeordersystem/order/service/OrderServiceLockTest.java`
- `docs/testing/evidence/issue-109/`

### Reverification

- `commands.md`와 `verification.md`의 Level 1, 3, 4, 5, 6 PASS를 확인했습니다.

### Next Attempt

- 없음.
