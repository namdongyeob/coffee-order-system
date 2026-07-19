# Issue Attempt Log

Issue: #132
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/132
Branch: codex/issue-132-ranking-rebuild-fence
Current disposition: PASS
Current Attempt: 1
Current head: edf3984688fbfe5efb9a9e3753da48f71dfbf08e

## Attempt 1

### Generate

- 기존 recovery lock을 normal processor도 공유하게 하고, lock 경합은 전용 retryable 예외로 구분했습니다.
- Kafka error handler는 fence 예외에만 container pause와 무제한 fixed backoff를 적용하고 기존 일반 오류의 2회 retry 후 DLT 계약은 유지했습니다.
- rebuild는 offset capture 뒤 runId·partition·offset을 기록하고, ledger prepare 뒤 swap 직전에 normal group을 다시 확인해 late join이면 run을 취소하고 중단하도록 했습니다.
- 실제 Testcontainers interleaving 3개와 processor/consumer focused 회귀를 TDD로 추가했습니다.

### Evaluate

- production 변경 전 첫 late-join 테스트는 offset E consumer가 rebuild lock 중에도 ledger를 생성해 `expected 0 but was 1`로 RED였습니다.
- fence 예외를 기존 bounded handler에만 연결한 단계에서는 3회 시도 뒤 DLT로 이동해 원 offset commit이 되지 않는 RED를 확인했습니다.
- 모든 수정 뒤 late-join·rebuild·normal ledger·DLT·bilateral recovery focused 50 tests가 failures/errors/skipped 0으로 PASS했습니다.
- 실제 Compose MySQL·Kafka·Redis와 별도 jar 프로세스에서 lock 보유 중 mutation 0, lock 해제 뒤 score 1·ledger COMMITTED·offset lag 0, active group rebuild 차단, consumer 종료 뒤 rebuild COMPLETED를 확인했습니다.

### Failure Cause

- 기능 blocker는 없습니다.
- 한글 parent 경로에서 Gradle worker가 테스트 class를 찾지 못하는 환경 문제가 있어 같은 worktree의 ASCII junction에서 재실행했습니다.
- 첫 Level 5 수동 publish는 Spring Kafka type header가 없는 raw JSON을 넣어 deserialization 오류가 발생한 잘못된 fixture였습니다. 환경을 volume부터 초기화하고 실제 serializer와 동일한 `__TypeId__` header로 재실행했습니다.
- consumer 종료 직후 첫 rebuild 시도는 Kafka session이 아직 active member를 보고 정확히 fail-closed 했습니다. group member 0을 확인한 뒤 성공 경로를 실행했습니다.

### Change Scope

- 허용된 ranking consumer/error handler/rebuild service와 직접 unit·integration test, `docs/testing/evidence/issue-132/**`만 변경했습니다.
- DLT replay 로직, Redis marker TTL, topic/payload/partition, ranking 계산 정책, production 전용 test hook은 변경하지 않았습니다.

### Reverification

- Reverification end: 2026-07-19T16:46:05+09:00.
- focused Level 1·3·4: 44 tests PASS, failures/errors/skipped 0, `BUILD SUCCESSFUL in 2m 34s`.
- 정확한 package의 normal ledger/marker Level 3·4 보완: 6 tests PASS, failures/errors/skipped 0, `BUILD SUCCESSFUL in 1m 13s`.
- 실제 Compose Level 5: lock 경합, release 후 1회 적용, active member fail-closed, member 0 rebuild success와 cleanup PASS.

### Next Attempt

없음. draft PR에서 fresh Review·QA와 최신 PR-head CI를 확인합니다.
