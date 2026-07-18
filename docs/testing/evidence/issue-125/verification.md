# 검증 로그

Attempt: 1
Head: bd7f6e279f4746783546b73e70a7a5a92e40d7c3

| 날짜 | Issue | Level | 결과 | 검증 범위 | 명령/Evidence | 비고 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-18 | Issue #125 ranking ledger retention | Level 0 | PASS | scope, diff, 운영 문서, evidence, cleanup receipt | `commands.md`, `manual-qa.md` | 금지 범위 변경 없음 |
| 2026-07-18 | Issue #125 ranking ledger retention | Level 1 | PASS | fixed Clock·설정 unit, focused와 전체 clean 회귀 | `commands.md` | 전체 139/139 |
| 2026-07-18 | Issue #125 ranking ledger retention | Level 3 | PASS | MySQL cutoff/state/rebuild/batch/retry/concurrency/predicate/index/EXPLAIN | `RankingLedgerCleanupIntegrationTest`, `commands.md` | V7, range 접근 |
| 2026-07-18 | Issue #125 ranking ledger retention | Level 4 | PASS | Redis TTL, Kafka DLT와 ADR-008/#119 bilateral 회귀 | `commands.md`, `manual-qa.md` | 기존 9 tests 포함 |
| 2026-07-18 | Issue #125 ranking ledger retention | Level 5 | PASS | 실제 Compose scheduler bounded 삭제·pending/marker 보존·invalid startup | `manual-qa.md` | `2 -> 1 -> 0`, fail-closed |

Level 5 required: YES, Level 6 required: NO. Level 6은 HTTP 경로가 없어 실행하지 않았습니다. 독립 Review·QA와 최신 PR-head CI는 draft PR에서 후속 확인합니다.
