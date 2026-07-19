# Issue Attempt Log

Issue: #132
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/132
Branch: codex/issue-132-ranking-rebuild-fence
Current disposition: PASS
Current Attempt: 2
Current head: d5f5bb55d236350c7055d6f69b5611cdd24a956a

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

- 최초 Review의 owner/run 관찰성 P1과 결정적 경쟁 테스트 P1을 허용된 Attempt 2에서 처리합니다.

## Attempt 2

### Generate

- Review P1 #1에 따라 rebuild runId를 lock 획득 전에 만들고 token을 `owner=REBUILD,runId=<UUID>`로 구조화했습니다.
- normal processor token은 `owner=CONSUMER,eventId=<UUID>,attemptId=<UUID>`로 구조화하고, lock 실패 시 Redis의 실제 owner를 fence 예외와 consumer 로그에 전달했습니다.
- rebuild 획득 실패는 `SHARED_RECOVERY_LOCK_BUSY`로 분류해 attemptedRunId와 실제 lockOwner를 함께 기록하며 더 이상 consumer owner를 다른 rebuild로 단정하지 않습니다.
- Review P1 #2에 따라 test-only `@MockitoSpyBean`과 latch로 실제 consumer `acquire()` 실패를 동기화하고 임의 1.5초/5초 sleep을 제거했습니다.
- 기존 세 순서에 release 전 원 offset 미커밋·DLT end 불변을 추가하고 consumer-first→rebuild-second 순서를 신규 검증했습니다.

### Evaluate

- unit RED는 새 owner 인자를 받는 fence 예외와 processor Redis owner 조회 생성자가 없어 compile 2건으로 실패했습니다.
- production 최소 수정 뒤 owner/log focused unit 13건이 PASS했습니다.
- 결정적 late-join 4건은 processor fence attempt/owner latch, offset, DLT와 최종 ledger·marker·score를 모두 확인해 PASS했습니다.
- focused Level 3/4 첫 52건 중 기존 lock message 호환 1건이 실패했고, owner를 rebuild로 단정하지 않으면서 `이미 실행 중` 계약을 유지해 최종 52/52 PASS했습니다.
- 실제 Level 5 consumer-first와 rebuild-first에서 structured owner/run이 양쪽 로그에 연결되고 두 이벤트 모두 최종 score 1·ledger COMMITTED·lag 0·DLT 0으로 수렴했습니다.

### Failure Cause

- P1 #1 원인은 Redis lock 값이 opaque UUID이고 rebuild runId를 획득 뒤 별도로 만들어 shared owner와 run을 연결할 수 없었던 것입니다.
- P1 #2 원인은 member 존재와 임의 sleep만으로 processor의 실제 lock 실패, offset 미커밋과 DLT 미발행을 증명하지 못했던 것입니다.
- focused 조합 실행에서 shared topic이 2 partitions로 확장되면 test helper가 assignment 1을 고정해 실패했습니다. production partition을 바꾸지 않고 실제 topic partition 수를 조회하도록 test-only helper를 수정했습니다.
- 기능 blocker는 없습니다.

### Change Scope

- 허용된 consumer/processor/fence exception/rebuild service와 직접 unit·late-join integration test, Issue #132 evidence만 수정했습니다.
- `RankingRebuildLock`, DLT replay, marker TTL/pending ledger, topic/payload/partition, ranking 정책과 ADR 문서는 변경하지 않았습니다.

### Reverification

- Reverification end: 2026-07-19T18:07:57+09:00.
- owner/log focused unit: 13 tests PASS, `BUILD SUCCESSFUL in 36s`.
- deterministic late-join: 4 tests PASS, `BUILD SUCCESSFUL in 1m 34s`.
- final Level 1·3·4 focused: 52 tests PASS, failures/errors/skipped 0, `BUILD SUCCESSFUL in 2m 45s`.
- actual Compose Level 5 consumer-first·rebuild-first owner/run 관찰성과 최종 ledger/marker/offset/score·DLT 정합 및 cleanup PASS.

### Next Attempt

없음. 최신 PR head에서 fresh Review와 CI를 확인합니다. 고정 head `8eaa526`의 독립 QA PASS는 production 변경으로 stale입니다.
