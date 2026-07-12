# Issue #21 Attempt Log

Issue: #21
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/21
Branch: codex/issue-21-point-concurrency

## Attempt 1

### Generate

- 시작 시각: `2026-07-12T18:58:41.6639074+09:00`.
- 기존 row와 missing row 각각에 같은 사용자 동시 충전 10건을 실행하는 MySQL Testcontainers 테스트를 먼저 추가했습니다.

### Evaluate

- RED: 기존 row는 총액을 보존했지만 missing row 최초 생성 경쟁은 `CannotAcquireLockException`과 MySQL transaction rollback으로 실패했습니다.
- 첫 GREEN 후보인 `insert ignore` 뒤 lock 조회는 기존 row에서도 lock conversion deadlock을 만들어 focused 테스트 2건이 실패했습니다.

### Failure Cause

- row가 없을 때 여러 트랜잭션이 동시에 insert하면서 unique index와 gap/row lock이 경합했습니다.
- 같은 트랜잭션에서 `insert ignore` 뒤 쓰기 락으로 승격하는 방식도 동시 요청 사이의 lock conversion deadlock을 제거하지 못했습니다.

### Change Scope

- 포인트 충전 서비스, 동시성 통합 테스트와 포인트 정책만 변경합니다.

### Reverification

- 실패한 설계를 유지하지 않고 새 트랜잭션 단위 재시도로 교체했습니다.

### Next Attempt

- unique 제약 충돌 또는 lock acquisition 실패만 최대 3회 새 트랜잭션에서 재조회·재시도합니다.

## Attempt 2

### Generate

- `TransactionTemplate`으로 한 번의 충전 시도를 독립 트랜잭션으로 만들고 충돌 시 전체 시도를 다시 실행했습니다.

### Evaluate

- GREEN: 기존 row와 missing row 동시 충전 테스트를 포함한 focused 5건이 모두 통과했습니다.
- 관련 14건과 전체 50건도 통과했습니다.

### Failure Cause

- 관련 테스트 첫 실행에서 `deleteAll()`이 다른 통합 테스트가 저장한 주문 FK와 충돌했습니다. 공유 DB 데이터를 전체 삭제하지 않고 각 테스트가 고유 userId를 사용하도록 테스트 격리를 바로잡았습니다.

### Change Scope

- 최초 Attempt와 동일하며 주문·Redisson·k6 범위는 변경하지 않았습니다.

### Reverification

- focused 5 tests PASS, 관련 14 tests PASS, 전체 50 tests PASS입니다.
- repository gate와 `git diff --check`가 PASS했습니다.
- 종료 시각: `2026-07-12T19:13:09.6305437+09:00`.

### Next Attempt

- 없음.
