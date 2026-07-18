# 검증 로그

Attempt: 2
Head: da96594416d5286ea9a7e2675c5f5d316a2e5470

| 날짜 | Issue | Level | 결과 | 검증 범위 | 명령/Evidence | 비고 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-18 | Issue #125 ranking ledger retention | Level 0 | PASS | scope, diff, 운영 문서, evidence, cleanup receipt | `commands.md`, `manual-qa.md` | 금지 범위 변경 없음 |
| 2026-07-18 | Issue #125 ranking ledger retention | Level 1 | PASS | Attempt 1 fixed Clock·설정 unit, focused와 전체 clean 회귀 | `commands.md` | production head `bd7f6e2`, 전체 139/139 |
| 2026-07-18 | Issue #125 ranking ledger retention | Level 3 | PASS | MySQL cutoff/state/rebuild/batch/retry/concurrency/predicate/index/EXPLAIN | `RankingLedgerCleanupIntegrationTest`, `commands.md` | V7, range 접근 |
| 2026-07-18 | Issue #125 ranking ledger retention | Level 4 | PASS | Redis TTL, Kafka DLT와 ADR-008/#119 bilateral 회귀 | `commands.md`, `manual-qa.md` | 기존 9 tests 포함 |
| 2026-07-18 | Issue #125 ranking ledger retention | Level 5 | PASS | 실제 Compose scheduler bounded 삭제·pending/marker 보존·invalid startup | `manual-qa.md` | `2 -> 1 -> 0`, fail-closed |
| 2026-07-18 | Issue #125 ranking ledger retention Attempt 2 | Level 1 | PASS | default disabled, enabled external window 필수 context | `commands.md` | current production head targeted PASS |
| 2026-07-18 | Issue #125 ranking ledger retention Attempt 2 | Level 3 | PASS | production 후보 SQL과 동일한 EXPLAIN | `commands.md` | cleanup index force, range 접근 |

Level 5 required: YES, Level 6 required: NO. Level 6은 HTTP 경로가 없어 실행하지 않았습니다. Attempt 2는 P1/P2 targeted만 재실행했고 전체 회귀는 최신 evidence-only PR head CI가 소유합니다. 독립 Review·QA와 CI는 후속 확인합니다.
