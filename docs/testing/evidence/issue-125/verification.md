# 검증 로그

Attempt: 3
Head: 2c1c378dc50380119f2728daa4859f982a5cae62

| 날짜 | Issue | Level | 결과 | 검증 범위 | 명령/Evidence | 비고 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-18 | Issue #125 ranking ledger retention | Level 0 | PASS | scope, diff, 운영 문서, evidence, cleanup receipt | `commands.md`, `manual-qa.md` | 금지 범위 변경 없음 |
| 2026-07-18 | Issue #125 ranking ledger retention | Level 1 | PASS | Attempt 1 fixed Clock·설정 unit, focused와 전체 clean 회귀 | `commands.md` | production head `bd7f6e2`, 전체 139/139 |
| 2026-07-18 | Issue #125 ranking ledger retention | Level 3 | PASS | MySQL cutoff/state/rebuild/batch/retry/concurrency/predicate/index/EXPLAIN | `RankingLedgerCleanupIntegrationTest`, `commands.md` | V7, range 접근 |
| 2026-07-18 | Issue #125 ranking ledger retention | Level 4 | PASS | Redis TTL, Kafka DLT와 ADR-008/#119 bilateral 회귀 | `commands.md`, `manual-qa.md` | 기존 9 tests 포함 |
| 2026-07-18 | Issue #125 ranking ledger retention | Level 5 | PASS | 실제 Compose scheduler bounded 삭제·pending/marker 보존·invalid startup | `manual-qa.md` | `2 -> 1 -> 0`, fail-closed |
| 2026-07-18 | Issue #125 ranking ledger retention Attempt 2 | Level 1 | PASS | default disabled, enabled external window 필수 context | `commands.md` | current production head targeted PASS |
| 2026-07-18 | Issue #125 ranking ledger retention Attempt 2 | Level 3 | PASS | production 후보 SQL과 동일한 EXPLAIN | `commands.md` | cleanup index force, range 접근 |
| 2026-07-18 | Issue #125 ranking ledger retention Attempt 3 | Level 1 | PASS | disabled core safety, sub-second marker fail-closed, enabled external window context | `commands.md` | focused properties/configuration 7건 |
| 2026-07-18 | Issue #125 ranking ledger retention Attempt 3 | Level 3 | PASS | 실패 CI cleanup 동시성 직접 재현 후 회귀 | `commands.md` | focused 동시성 1건 |

Level 5 required: YES, Level 6 required: NO. Level 6은 HTTP 경로가 없어 실행하지 않았습니다. Attempt 3는 두 P1과 실패 CI 직접 원인만 targeted 재실행했고 전체 141 회귀는 최신 evidence-only PR head CI가 소유합니다. 독립 Review·QA와 CI는 후속 확인합니다.
