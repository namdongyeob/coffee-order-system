# 검증 로그

Attempt: 3
Head: f33b054

| 날짜 | Issue | Level | 결과 | 검증 범위 | 명령/Evidence | 비고 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-16 | Issue #112 Rebuild ledger 연동 | Level 0 | PASS | issue diff, migration schema, fingerprint unit, secret·large-file·scope 검사 | `commands.md`, `attempt-log.md` | DltReplayService·정상 consumer production·Redis marker 변경 0개입니다. |
| 2026-07-16 | Issue #112 Rebuild ledger 연동 | Level 1 | PASS | Rebuild 14 tests, 관련 Ranking·PopularMenu 43 tests, 전체 99 tests | `commands.md` | 모두 failures=0, errors=0, skipped=0입니다. |
| 2026-07-16 | Issue #112 Rebuild ledger 연동 | Level 3 | PASS | Flyway V6와 MySQL ledger/run/run-event 저장·복구 | `manual-qa.md` | 최초·동일 재실행·pending 복구 DB 상태를 조회했습니다. |
| 2026-07-16 | Issue #112 Rebuild ledger 연동 | Level 4 | PASS | Kafka replay offset, Redis atomic ranking, 중복·충돌·pending 장애 복구 | `commands.md`, `manual-qa.md` | 동일 이벤트 score 1, ledger 1, pending 복구 input 0, lag 0입니다. |
| 2026-07-16 | Issue #112 Rebuild ledger 연동 | Level 5 | PASS | 실제 Compose MySQL·Kafka·Redis와 로컬 앱의 주문→이벤트→Rebuild 실행 | `manual-qa.md` | health 503은 subst disk 관측으로 분리 기록했고 기능 E2E는 PASS했습니다. |

Level 5 required: YES, Level 6 required: NO. 독립 Review·QA와 최신 CI는 GitHub 정본에서 후속 확인합니다.
