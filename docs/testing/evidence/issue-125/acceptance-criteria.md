# Issue #125 Acceptance Criteria

Issue: #125
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/125

Execution mode: STRICT
Execution mode reason: DB migration·ledger 삭제 계약, scheduler, Redis marker TTL, Kafka·DLT retention 및 rebuild recovery window 정합성을 함께 변경·검증하는 recovery 작업입니다.
Level 5 required: YES
Level 5 reason: 실제 MySQL·Redis·Kafka 연결에서 scheduler 기동, bounded 삭제, fail-closed 설정과 종료 상태를 확인해야 합니다.
Level 6 required: NO
Level 6 reason: retention은 직접 호출하는 HTTP API가 없는 내부 maintenance 경로이며 HTTP 계약을 변경하지 않습니다.

## Dev 완료 기준

- [x] cutoff보다 오래된 독립 `COMMITTED` 행만 삭제됩니다.
- [x] cutoff 바로 전 행은 삭제되고, 정확히 cutoff 및 cutoff 이후 행은 유지됩니다.
- [x] 오래된 `RESERVED`, `REDIS_APPLIED` 행은 유지됩니다.
- [x] `PREPARED`, `SWAPPED_PENDING_OFFSET`, `OFFSET_APPLIED_PENDING_LEDGER`, `RECOVERY_REQUIRED` rebuild 행은 유지됩니다.
- [x] `COMPLETED` rebuild에 연결된 오래된 `COMMITTED` 행은 삭제 대상입니다.
- [x] 적격 행이 batch 크기보다 많아도 한 실행의 삭제 수가 batch 상한을 넘지 않습니다.
- [x] 반복 실행은 안전하며 최종 실행은 0건으로 종료됩니다.
- [x] 동시 cleanup에서 각 batch 상한, 예외 부재, 실제 삭제 수와 잔여 수 정합성을 확인했습니다.
- [x] cleanup은 Redis marker를 `SCAN`하거나 직접 일괄 삭제하지 않습니다.
- [x] Redis marker TTL이 ledger retention 이상인지 fail-closed로 검증하고 실제 Redis TTL을 확인했습니다.
- [x] Kafka, DLT 또는 최대 rebuild recovery window가 ledger retention보다 길거나 알 수 없으면 삭제 전에 fail-closed합니다.
- [x] V7 `(state, committed_at)` 인덱스와 MySQL `EXPLAIN`의 `range` 접근 근거가 있습니다.
- [x] 보존 기간, batch 크기, 실행 주기, effective retention 확인과 fail-closed 운영 방법을 문서화했습니다.
- [x] ADR-008 eventId 최대 1회 반영과 #119 DLT/Rebuild 양방향 회귀 9건을 재검증했습니다.
- [x] Issue evidence 6종, PR body preflight 입력과 최신 PR-head CI 실행 준비를 완료했습니다.

## STRICT 후속 게이트

draft PR 뒤 fresh Review, independent QA Level 3·4·5, 최신 PR-head GitHub Actions `quality-gates` PASS는 Main Coordinator가 후속 확인합니다. 이 문서의 Dev PASS는 해당 독립 gate를 대체하지 않습니다.
