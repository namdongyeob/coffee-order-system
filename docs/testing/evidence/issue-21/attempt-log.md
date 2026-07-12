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
- Fresh Review에서 기본 `TransactionTemplate`의 `PROPAGATION_REQUIRED`가 상위 트랜잭션에 참여해 재시도 경계를 무너뜨릴 수 있다는 P1 한 건을 발견했습니다.
- 상위 트랜잭션 rollback 뒤 충전도 함께 rollback되는 focused RED를 실제 MySQL에서 재현한 뒤 전용 template에 `PROPAGATION_REQUIRES_NEW`를 지정했습니다.

### Failure Cause

- 관련 테스트 첫 실행에서 `deleteAll()`이 다른 통합 테스트가 저장한 주문 FK와 충돌했습니다. 공유 DB 데이터를 전체 삭제하지 않고 각 테스트가 고유 userId를 사용하도록 테스트 격리를 바로잡았습니다.

### Change Scope

- 최초 Attempt와 동일하며 주문·Redisson·k6 범위는 변경하지 않았습니다.

### Reverification

- remediation propagation focused 1 test PASS, 전체 focused 6 tests PASS, 관련 15 tests PASS입니다.
- 전체 회귀 첫 실행은 Kafka Testcontainer startup timeout으로 context가 실패했지만 제품 assertion 실패는 없었습니다. 코드 변경 없이 fresh rerun한 전체 51 tests가 PASS했습니다.
- repository gate와 `git diff --check`가 PASS했습니다.
- 종료 시각: `2026-07-12T19:35:16.4749098+09:00`.

### Next Attempt

- 없음.

## Docs Final Sync

### Generate

- Docs 시작 시각: `2026-07-12T19:59:15.1121911+09:00`.
- Current head: `ce0be41691a7019b27d486ab969a188e7e1353c4`.
- 입력: live Issue #21, PR #73 본문, Dev evidence, remediation 전·후 Review와 QA 댓글을 대조했습니다.

### Change Scope

- `docs/testing/evidence/issue-21/`의 기본 evidence 5개와 `docs/testing/verification-log.md`만 최종 동기화했습니다.
- GitHub의 head, Review·QA·CI·merge 가변 상태는 저장소 evidence에 복제하지 않았습니다.

### Reverification

- 독립 QA의 Level 3 focused 6건, 관련 15건, 전체 51건 PASS를 반영했습니다.
- Review P1 1건과 PR-body metadata QA 결함 1건을 서로 다른 결함 유형으로 기록했습니다.
- Docs 종료 시각: `2026-07-12T19:59:59.9798838+09:00`.

### Next Attempt

- 없음.
