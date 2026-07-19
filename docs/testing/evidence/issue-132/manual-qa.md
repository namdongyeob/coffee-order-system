# Issue #132 Manual QA

Issue: #132
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/132
Date: 2026-07-19

## 실제 환경

- `docker/compose.yaml`의 MySQL 8.4.5, Kafka 3.9.1, Redis 7.4.2를 빈 volume에서 기동해 모두 healthy인 상태로 검증했습니다.
- `bootJar` 산출물을 normal consumer 프로세스와 maintenance rebuild 프로세스로 각각 실행했습니다.

## Recovery lock 중 normal event

- 정상 consumer group member 1을 확인한 뒤 공용 `ranking:rebuild:lock`을 외부에서 획득하고 typed `OrderCompletedEvent`를 offset 0에 게시했습니다.
- lock 보유 5초 동안 `ranking_event_ledger` 행 0, applied marker 없음, live score 없음이었고 consumer member는 active로 유지됐습니다.
- lock 해제 뒤 같은 offset이 처리되어 ledger는 `COMMITTED/NORMAL_CONSUMER`, marker는 event fingerprint, score는 정확히 1, group current/end/lag는 `1/1/0`이 됐습니다.

## Rebuild 시작 fence와 성공 경로

- normal member 1인 동안 maintenance runner는 runId와 `reason=ACTIVE_NORMAL_CONSUMER`, `phase=START`, `memberCount=1`을 기록하고 swap 전에 종료했습니다.
- consumer 프로세스 종료 직후 Kafka session이 잠시 active인 첫 재시도도 동일하게 차단됐습니다. 이는 stale session을 안전하다고 추정하지 않는 fail-closed 결과입니다.
- group member 0 확인 후 재실행한 rebuild는 partition 0의 captured offset 1을 기록하고 `inputRecords=1 uniqueEvents=1 conflicts=0`으로 완료했습니다.
- 최종 rebuild run은 `COMPLETED`, score는 1, recovery lock과 `ranking:rebuild:*` 임시 key는 0이었습니다.

## Automated interleaving QA

- capture 뒤 offset E consumer가 실제 처리 시도한 상태에서 swap 직전 member가 남아 있으면 rebuild run이 취소되고 consumer가 lock 해제 뒤 E를 정확히 한 번 commit합니다.
- offset E 처리 시도 뒤 member가 떠나면 rebuild가 captured E로 swap/offset 이동을 완료하고, consumer 재시작 뒤 E가 한 번만 적용됩니다.
- recovery lock 실패 5회를 latch로 관찰해 `max.poll.interval.ms=3000`을 넘겨도 container pause로 member를 유지하고 DLT 없이 원 offset을 재시도합니다.

## Adversarial QA

- 일반 처리 오류는 기존 1초 간격 2회 retry 뒤 DLT로 이동하는 회귀를 통과했습니다.
- Redis WRONGTYPE 실패는 marker-only를 남기지 않고 retry 후 score 1·ledger COMMITTED가 됩니다.
- rebuild/DLT bilateral, 다른 fingerprint duplicate, incomplete rebuild recovery focused 회귀를 함께 통과했습니다.
- raw JSON without type header로 발생한 deserialization 반복은 잘못된 수동 fixture로 판별해 volume을 초기화하고 실제 producer와 동등한 type header로 재검증했습니다.

## Attempt 2 structured owner/run Level 5

- consumer-first에서는 `ranking_event_ledger` table lock으로 actual processor가 recovery lock을 보유하게 했습니다. Redis owner는 `owner=CONSUMER,eventId=13200000-...0002,attemptId=<UUID>`였습니다.
- 같은 동안 실행한 rebuild는 `reason=SHARED_RECOVERY_LOCK_BUSY attemptedRunId=842b597b-... lockOwner=<동일 consumer owner>`를 기록하고 run/swap 없이 종료했습니다.
- release 전 marker·score가 없고 offset은 `0/1/1`, DLT topic은 없었습니다. release 뒤 ledger `COMMITTED/NORMAL_CONSUMER`, marker fingerprint, score 1, offset `1/1/0`이 됐습니다.
- rebuild-first에서는 `ranking_rebuild_run` table lock으로 actual rebuild를 capture 전 정지했습니다. acquire log의 `runId=ad8add8d-...`와 `lockOwner=owner=REBUILD,runId=ad8add8d-...`가 일치했습니다.
- offset 1 consumer 차단 로그도 같은 rebuild owner를 기록했습니다. release 전 marker·score가 없고 offset은 `1/2/1`, DLT topic은 없었습니다.
- consumer 종료와 DB lock release 뒤 PRE_SWAP은 남은 active member를 감지해 같은 runId로 abort했고 run plan 0을 유지했습니다. consumer는 event를 최종 `COMMITTED/NORMAL_CONSUMER`, marker, score 1, offset `2/2/0`으로 완료했습니다.

## Cleanup receipt

- `docker compose -f docker/compose.yaml down -v --remove-orphans`를 실행했습니다.
- compose project container/network/volume, Issue #132 jar Java 프로세스, 8080/13306/16379/19092 listener가 남지 않았습니다.
- Level 5 stdout/stderr 임시 로그 4개를 삭제했고 모두 `Test-Path=False`를 확인했습니다.
- Gradle Unicode 우회용 ASCII junction과 빈 parent 디렉터리를 최종 preflight 뒤 삭제했고 실제 worktree는 유지됨을 확인했습니다.
- Attempt 2 Compose container/network/volume, jar Java 프로세스와 8080/13306/16379/19092 listener도 0으로 정리했습니다.
- Attempt 2 stdout/stderr 임시 로그 8개를 삭제하고 잔여 0을 확인했습니다.
- Attempt 2 ASCII junction과 빈 parent를 삭제하고 실제 worktree가 유지됨을 확인했습니다.

## 남은 위험과 후속 게이트

- fence 경합은 1초 고정 pause-retry이므로 장시간 rebuild 동안 처리 지연이 발생하지만 consumer thread sleep이나 poll 계약 위반 없이 record를 보존합니다.
- maintenance runner가 완료 로그 뒤 Spring context를 자동 종료하지 않는 기존 동작 때문에 Level 5 성공 확인 후 해당 runner PID를 종료했습니다. rebuild 데이터·lock 상태에는 영향이 없었습니다.
- 독립 Review·QA와 최신 PR-head GitHub Actions CI는 draft PR에서 후속 확인합니다.
- 고정 head `8eaa526`의 독립 QA는 PASS였지만 Attempt 2 production 변경 뒤에는 stale이며 최신 head 검증에 사용하지 않습니다.
