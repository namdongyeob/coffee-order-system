# Issue Attempt Log

Issue: #112
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/112
Branch: issue-112-attempt2
Current disposition: PASS
Current Attempt: 3
Current head: f33b054

## Attempt 1

### Generate

- Rebuild replay 이벤트를 `ranking_event_ledger`와 run/run-event 테이블에 기록하는 V6 migration과 fingerprint 모델을 추가했습니다.
- swap 전 run 준비, swap 후 pending 전환, offset 이동 뒤 backfill·완료 흐름을 Rebuild 서비스에 연결했습니다.
- 동일 payload 중복과 fingerprint schema focused 테스트를 추가했습니다.

### Evaluate

- PASS. fingerprint/schema quick test는 `BUILD SUCCESSFUL in 34s`였습니다.
- 환경 RED. 첫 통합 실행이 멈춰 thread dump를 확인했고, 테스트 worker와 Kafka start async thread의 class monitor 교착을 확인했습니다.
- PASS. `clearSharedKafkaTopics()`의 불필요한 중첩 class monitor를 제거한 뒤 기존 shared lifecycle 테스트가 `BUILD SUCCESSFUL in 1m 10s`였습니다.
- PASS. 초기 ledger focused 2 tests는 `BUILD SUCCESSFUL in 1m 24s`였습니다.

### Failure Cause

- production 기능 문제가 아니라 #113에서 도입된 test-only singleton start가 class monitor를 보유한 상태에서, async Kafka start가 같은 monitor의 topic clear에 진입하려 해 발생한 교착이었습니다.

### Change Scope

- production 변경은 Rebuild ledger 모델·서비스·V6 migration에 한정했습니다.
- test-only 변경은 `SharedTestcontainers.clearSharedKafkaTopics()`의 중첩 `synchronized` 제거와 Rebuild 통합 테스트입니다.

### Reverification

- thread dump로 양쪽 stack과 동일 monitor 대기를 확인했습니다.
- `SharedTestcontainersLifecycleIntegrationTest`와 ledger focused 2 tests를 ASCII subst 경로에서 재실행해 PASS했습니다.

### Next Attempt

swap 전 충돌 차단과 pending 재시도에서 새 run·swap이 생기지 않는지 실패 주입 테스트로 확장합니다.

## Attempt 2

### Generate

- 기존 ledger의 다른 fingerprint를 swap 전에 검증하는 fail-closed 테스트를 추가했습니다.
- swap 후 backfill 실패를 주입하고 lock 보존, lease 만료 모사, 같은 pending run 복구를 검증했습니다.
- RED를 확인한 뒤 pre-swap validation과 pending recovery short-circuit를 구현했습니다.

### Evaluate

- RED. 충돌 시 live ranking이 이미 변경됐고 pending retry가 두 번째 run을 생성해 focused 2 tests가 실패했습니다.
- GREEN. 수정 뒤 같은 2 tests가 `BUILD SUCCESSFUL in 1m 35s`였습니다.
- PASS. Rebuild 통합 14 tests는 `BUILD SUCCESSFUL in 1m 33s`였습니다.
- PASS. 관련 Ranking·PopularMenu 묶음 43 tests는 `BUILD SUCCESSFUL in 2m 8s`였습니다.

### Failure Cause

- 기존 fingerprint 비교가 swap 뒤에 있어 충돌이 live ranking을 건드렸고, pending recovery 후에도 일반 replay 흐름을 계속해 새 run을 만들었습니다.

### Change Scope

- Rebuild ledger의 prevalidation과 pending recovery 반환값, Rebuild 서비스의 recovery 우선 종료만 수정했습니다.
- DLT replay와 정상 consumer production 코드는 수정하지 않았습니다.

### Reverification

- 충돌·pending focused 2 tests GREEN, Rebuild 14 tests PASS, 관련 43 tests PASS를 확인했습니다.

### Next Attempt

실제 Compose Level 5에서 최초·동일 재실행·pending 복구를 확인하고 DB 시간 정밀도 경계를 검증합니다.

## Attempt 3

### Generate

- 실제 주문 이벤트를 Kafka에 발행하고 Rebuild runner를 두 번 실행해 ledger와 Redis 중복 방지를 검증했습니다.
- 첫 Level 5 동일 재실행에서 MySQL `datetime(6)` 정밀도 손실로 false fingerprint conflict가 발생하는 것을 확인했습니다.
- orderedAt nanoseconds를 포함하도록 통합 테스트를 강화해 RED를 재현하고, backfill 시 run-event에 저장된 원본 fingerprint를 사용하도록 수정했습니다.
- 실제 pending run을 조성해 다음 실행이 replay·swap 없이 같은 run을 완료하는지 확인했습니다.

### Evaluate

- RED. nanoseconds `123456789` 이벤트의 동일 재실행 테스트가 false conflict로 실패했습니다.
- GREEN. 저장된 fingerprint 사용 뒤 focused 1 test가 `BUILD SUCCESSFUL in 1m 33s`였습니다.
- PASS. Rebuild 14 tests와 fingerprint/schema tests가 `BUILD SUCCESSFUL in 1m 33s`였습니다.
- PASS. 관련 clean 묶음 43/43 tests가 `BUILD SUCCESSFUL in 2m 19s`였습니다.
- PASS. 전체 clean test 99/99가 `BUILD SUCCESSFUL in 3m 15s`였습니다.
- PASS. Level 5 최종 실행에서 최초 score 1·ledger 1, 동일 재실행 score 1·ledger 1, pending 복구 후 run 수 불변과 same run id backfill을 확인했습니다.

### Failure Cause

- Kafka payload의 `LocalDateTime` nanoseconds가 run-event DB의 `datetime(6)`에 저장될 때 microseconds로 줄어들었고, backfill이 DB payload로 fingerprint를 재계산해 원본 fingerprint와 달라졌습니다.

### Change Scope

- backfill은 run-event에 prepare 시 저장한 `payload_fingerprint`를 그대로 ledger에 기록하도록 변경했습니다.
- Rebuild 전용 production과 관련 테스트만 수정했으며 DLT replay, 정상 consumer production, Redis marker는 변경하지 않았습니다.

### Reverification

- focused RED→GREEN, Rebuild 14 tests, 관련 43 tests, 전체 99 tests가 모두 failures=0, errors=0, skipped=0으로 PASS했습니다.
- Level 5에서 event `ae9811d8-8868-4c96-afec-95af9d279db4`의 ledger fingerprint가 동일 재실행 뒤에도 한 종류·한 행이고 Redis score가 1임을 확인했습니다.
- pending 복구 실행은 `inputRecords=0`, `uniqueEvents=0`, `conflicts=0`으로 끝났고 run 수는 2에서 늘지 않았습니다.
- `git diff --cached --check`, 범위·secret·1MB 초과 파일 검사를 통과했습니다.

### Next Attempt

없음. Dev 단계는 종료했으며 독립 Review·QA와 최신 CI는 GitHub 정본에서 후속 확인합니다.
