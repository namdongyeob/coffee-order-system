# 검증 로그

Attempt: 4
Head: b3753c8

| 날짜 | Issue | Level | 결과 | 검증 범위 | 명령/Evidence | 비고 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-16 | Issue #112 Rebuild ledger 연동 | Level 0 | PASS | issue diff, migration schema, fingerprint unit, secret·large-file·scope 검사 | `commands.md`, `attempt-log.md` | DltReplayService·정상 consumer production·Redis marker 변경 0개입니다. |
| 2026-07-16 | Issue #112 Rebuild ledger 연동 | Level 1 | PASS | Rebuild 23 tests, 관련 Ranking·PopularMenu 46 tests, 전체 102 tests | `commands.md` | 모두 failures=0, errors=0, skipped=0입니다. |
| 2026-07-16 | Issue #112 Rebuild ledger 연동 | Level 3 | PASS | Flyway V6와 MySQL ledger/run/run-event/run-offset 저장·복구 | `manual-qa.md` | 최초·동일 재실행·swap marker/offset recovery DB 상태를 조회했습니다. |
| 2026-07-16 | Issue #112 Rebuild ledger 연동 | Level 4 | PASS | Redis atomic swap marker, captured Kafka offsets, 보상 분기, batch heartbeat | `commands.md`, `manual-qa.md` | same-run offset 0→1, incomplete 보상/renew 실패 pending 보존을 확인했습니다. |
| 2026-07-16 | Issue #112 Rebuild ledger 연동 | Level 5 | PASS | 실제 Compose MySQL·Kafka·Redis와 로컬 앱의 최초 rebuild 및 PREPARED marker 복구 | `manual-qa.md` | same run, input 0 recovery, current=end=1, lag 0, score 1을 확인했습니다. |

Level 5 required: YES, Level 6 required: NO. 독립 Review·QA와 최신 CI는 GitHub 정본에서 후속 확인합니다.
