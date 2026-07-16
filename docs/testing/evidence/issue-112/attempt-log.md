# Issue Attempt Log

Issue: #112
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/112
Branch: issue-112-attempt2
Current disposition: PASS
Current Attempt: 6
Current head: 3e25f27

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
- Rebuild 전용 production과 관련 테스트만 수정했으며 DLT replay, 정상 consumer production, 공통 applied-event marker는 변경하지 않았습니다.

### Reverification

- focused RED→GREEN, Rebuild 14 tests, 관련 43 tests, 전체 99 tests가 모두 failures=0, errors=0, skipped=0으로 PASS했습니다.
- Level 5에서 event `ae9811d8-8868-4c96-afec-95af9d279db4`의 ledger fingerprint가 동일 재실행 뒤에도 한 종류·한 행이고 Redis score가 1임을 확인했습니다.
- pending 복구 실행은 `inputRecords=0`, `uniqueEvents=0`, `conflicts=0`으로 끝났고 run 수는 2에서 늘지 않았습니다.
- `git diff --cached --check`, 범위·secret·1MB 초과 파일 검사를 통과했습니다.

### Next Attempt

없음. Dev 단계는 종료했으며 독립 Review·QA와 최신 CI는 GitHub 정본에서 후속 확인합니다.

## Attempt 4

### Generate

- 독립 Review의 P1 네 건에 대해 swap/DB mark, captured offset recovery, 보상 결과, lease heartbeat 상태 경계를 TDD로 보완했습니다.
- Redis Lua가 live/backup 교체와 `ranking:rebuild:swap:{runId}` marker를 원자 기록하고 marker·backup을 31일 보존하도록 변경했습니다.
- run에 namespace·날짜 window와 partition별 captured end·이전 offset을 저장하고 `SWAPPED_PENDING_OFFSET`, `OFFSET_APPLIED_PENDING_LEDGER`, `RECOVERY_REQUIRED` 상태를 추가했습니다.
- 완전 보상은 FK cascade 단일 run 삭제, 불완전 보상은 events·offset plan·lock 보존으로 분리했습니다.
- ledger backfill은 50행 batch마다 lease heartbeat를 실행합니다.

### Evaluate

- RED. swap 직후 DB mark 실패 테스트는 Redis run marker가 없어 실패했습니다.
- RED. schema 테스트는 durable offset table, 새 상태와 cascade가 없어 실패했습니다.
- RED. partial partition offset crash는 재실행이 captured end를 복구하지 못했습니다.
- RED. 불완전 보상은 run/events를 삭제했고 lock을 해제했습니다.
- RED. 101-event heartbeat 테스트는 backfill 중 renew가 호출되지 않아 예외가 발생하지 않았습니다.
- GREEN. 핵심 crash/offset/compensation/heartbeat 5 tests, Rebuild 전체 23/23, 관련 clean 46/46, 전체 clean 102/102가 PASS했습니다.
- GREEN. Level 5에서 실제 Compose V6, 최초 rebuild, PREPARED+swap marker+lag 1 same-run recovery가 PASS했습니다.

### Failure Cause

- 기존 흐름은 Redis swap과 DB `markSwapped` 사이에 durable marker가 없고 captured offsets를 run에 저장하지 않았습니다.
- offset 실패 외부 catch가 pending run을 일괄 discard했고, backfill loop에는 장시간 lease heartbeat가 없었습니다.

### Change Scope

- production은 Rebuild 전용 ledger/service와 아직 merge되지 않은 V6 migration만 수정했습니다.
- `DltReplayService`, 정상 ranking consumer production 코드, 공통 Redis applied-event marker는 수정하지 않았습니다.
- P2 manual QA의 모호한 표현을 “동일 Rebuild 재실행”으로 정확히 고쳤습니다.

### Reverification

- `*RankingRebuild*` — PASS, 23/23, BUILD SUCCESSFUL in 2m 31s.
- clean `*Ranking*` + `*PopularMenu*` — PASS, 46/46, BUILD SUCCESSFUL in 3m 2s.
- 전체 clean test — PASS, 102/102, BUILD SUCCESSFUL in 3m 21s.
- Level 5 최초 runner — input/unique/conflict 1/1/0, run COMPLETED, offset current=end=1, lag 0.
- Level 5 crash 조성 뒤 recovery — input/unique/conflict 0/0/0, run 총수 1·같은 runId, offset 0→1, ledger 복구, score 1.
- secret pattern 0, 1MB 초과 0, forbidden production scope 0, Compose/runner cleanup 완료.

### Next Attempt

없음. Review 재검증과 독립 QA, 최신 CI는 GitHub 정본에서 후속 확인합니다.

## Attempt 5

### Generate

- 재검토 P1 세 건에 따라 `OFFSET_APPLIED_PENDING_LEDGER` 복구를 offset verify와 ledger backfill 전용으로 분리하고, verify 실패 시 `RECOVERY_REQUIRED`로 봉인했습니다.
- Redis·offset 완전 보상 뒤 run cancel이 실패하면 같은 run을 `RECOVERY_REQUIRED`로 봉인해 자동 offset 이동과 ledger 완료를 차단했습니다.
- 대량 prepare의 50행 event batch와 offset plan 저장 사이에 lease heartbeat를 추가하고 atomic swap 직전에 소유권을 다시 확인했습니다.
- 위 경계를 검증하는 회귀 테스트 4개를 추가하고 기존 backfill heartbeat 테스트의 실패 주입 순서를 새 renew 호출 순서에 맞췄습니다.

### Evaluate

- RED. 신규 focused 4 tests가 부분 ledger 보존, cancel 실패 봉인, prepare 중 lease 상실, swap 직전 소유권 변경 경계에서 모두 실패했습니다.
- GREEN. 같은 focused 4 tests와 기존 backfill heartbeat focused test가 PASS했습니다.
- PASS. Rebuild 전체 27/27, 관련 clean 50/50, 전체 clean 106/106이 failures=0, errors=0, skipped=0으로 통과했습니다.
- Level 5는 사용자 지시에 따라 반복하지 않았으며 Attempt 4의 실제 Compose 결과를 유지했습니다.

### Failure Cause

- `SWAPPED_PENDING_OFFSET`과 `OFFSET_APPLIED_PENDING_LEDGER`가 같은 recovery 분기를 사용해 이미 적용된 offset을 다시 이동하고 실패 시 ledger 일부와 분리될 수 있었습니다.
- 완전 보상 뒤 cancel 실패를 durable 상태로 봉인하지 않았고, prepare와 atomic swap 사이에는 장시간 작업을 덮는 lease 재확인이 없었습니다.

### Change Scope

- Rebuild 전용 ledger/service와 Rebuild 통합 테스트만 수정했습니다.
- `DltReplayService`, 정상 ranking consumer production 코드, 공통 applied-event marker는 수정하지 않았습니다.

### Reverification

- focused 4/4, 기존 heartbeat 1/1, Rebuild 27/27, 관련 clean 50/50, 전체 clean 106/106이 PASS했습니다.
- `git diff --check`, 1MB 초과 파일과 forbidden production scope 검사를 통과했습니다.

### Next Attempt

없음. 재검토·독립 QA와 최신 CI는 갱신된 Draft PR의 GitHub 정본에서 후속 확인합니다.

## Attempt 6

### Generate

- 독립 Review P1에 따라 31일 TTL을 제거하고 run별 swap marker, backup, 날짜별 원래 live 존재 메타를 완료 또는 정상 cancel까지 무기한 보존했습니다.
- rollback Lua를 전체 recovery artifact 검증 단계와 live mutation 단계로 분리하고, 원래 live가 있던 날짜만 Redis `COPY`로 복원해 cancel 전 backup을 유지했습니다.
- PREPARED marker 소실과 SWAPPED backup·존재 메타 소실을 자동 진행하지 않고 `RECOVERY_REQUIRED`로 봉인했습니다.
- recovery wrapper가 `RunExecutionException.retainLock()`을 반영해 완전 보상·run cancel 뒤 lock을 해제하도록 수정했습니다.

### Evaluate

- RED. durable artifact/cleanup, PREPARED marker 소실, SWAPPED backup·존재 메타 소실, recovery lock 해제 focused 5 tests가 모두 실패했습니다.
- GREEN. 같은 focused 5 tests가 PASS했습니다.
- PASS. Rebuild 전체 31/31, 관련 clean 54/54, 전체 clean 110/110이 failures=0, errors=0, skipped=0으로 통과했습니다.
- PASS. 최신 코드 Level 5에서 intact PREPARED same-run recovery/cleanup과 backup 소실 fail-closed를 실제 Compose로 확인했습니다.

### Failure Cause

- marker와 backup의 31일 TTL이 무기한 DB pending보다 짧고, 원래 live가 없던 경우와 backup 소실을 구분하는 durable 메타가 없었습니다.
- rollback Lua가 날짜별 live를 먼저 삭제해 뒤 날짜의 artifact 소실을 발견하면 부분 mutation이 가능했습니다.
- recovery wrapper가 보상 결과와 무관하게 모든 runtime failure의 lock을 유지했습니다.

### Change Scope

- Rebuild 전용 service와 Rebuild Testcontainers 통합 테스트만 수정했습니다.
- `DltReplayService`, 정상 ranking consumer production 코드, 공통 applied-event marker는 수정하지 않았습니다.

### Reverification

- focused 5/5, Rebuild 31/31, 관련 clean 54/54, 전체 clean 110/110이 PASS했습니다.
- Level 5 intact recovery는 run/events 1/1, COMPLETED, input 0, current/end/lag 1/1/0, score 1, marker/meta/backup/lock 0이었습니다.
- Level 5 backup 소실은 run/events 1/1, RECOVERY_REQUIRED, ledger 1 COMMITTED/REBUILD, score 1, current/end/lag 1/1/0을 유지하고 lock 1·marker/meta TTL -1을 보존했습니다.
- `git diff --check`, 1MB 초과 파일과 forbidden production scope 검사를 통과했고 Compose container/network를 제거했습니다.

### Next Attempt

없음. 독립 re-review·QA와 최신 CI는 Ready PR의 GitHub 정본에서 후속 확인합니다.
