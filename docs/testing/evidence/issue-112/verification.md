# 검증 로그

Attempt: 6
Head: 3e25f27

| 날짜 | Issue | Level | 결과 | 검증 범위 | 명령/Evidence | 비고 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-16 | Issue #112 Rebuild ledger 연동 | Level 0 | PASS | issue diff, migration schema, fingerprint unit, secret·large-file·scope 검사 | `commands.md`, `attempt-log.md` | DltReplayService·정상 consumer production·공통 applied-event marker 변경 0개입니다. |
| 2026-07-16 | Issue #112 Rebuild ledger 연동 | Level 1 | PASS | Rebuild 31 tests, 관련 Ranking·PopularMenu 54 tests, 전체 110 tests | `commands.md` | 모두 failures=0, errors=0, skipped=0입니다. |
| 2026-07-16 | Issue #112 Rebuild ledger 연동 | Level 3 | PASS | Flyway V6와 MySQL ledger/run/run-event/run-offset 저장·복구 | `manual-qa.md` | 최초·동일 재실행·swap marker/offset recovery DB 상태를 조회했습니다. |
| 2026-07-16 | Issue #112 Rebuild ledger 연동 | Level 4 | PASS | Redis atomic swap marker·backup·존재 메타, two-phase rollback, captured Kafka offsets, retainLock 분기 | `commands.md`, `manual-qa.md` | artifact 소실 live/offset 불변, RECOVERY_REQUIRED·lock 보존과 정상 cancel cleanup·lock 해제를 확인했습니다. |
| 2026-07-16 | Issue #112 Rebuild ledger 연동 | Level 5 | PASS | 실제 Compose의 intact PREPARED same-run recovery/cleanup과 SWAPPED backup 소실 fail-closed | `manual-qa.md` | 정상 recovery는 artifact/lock 0, 소실 시 score 1·current/end/lag 1/1/0 불변과 RECOVERY_REQUIRED·lock 1·marker/meta TTL -1을 확인했습니다. |

Level 5 required: YES, Level 6 required: NO. 독립 Review·QA와 최신 CI는 GitHub 정본에서 후속 확인합니다.
