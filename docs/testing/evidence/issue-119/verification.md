# 검증 로그

Attempt: 2
Head: 8506132df37034e31ee2e8037eb6a37dead2050f

| 날짜 | Issue | Level | 결과 | 검증 범위 | 명령/Evidence | 비고 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-16 | Issue #119 ranking common ledger | Level 0 | PASS | scope, diff, fingerprint fail-closed, secret·large-file | `commands.md`, `attempt-log.md` | #112 Rebuild bulk 동작 변경 없음 |
| 2026-07-16 | Issue #119 ranking common ledger | Level 1 | PASS | focused 12, 관련 clean 76, 전체 clean 126 | `commands.md` | failures/errors/skipped 0 |
| 2026-07-16 | Issue #119 ranking common ledger | Level 3 | PASS | MySQL ledger durable states와 retry, schema V6 | `commands.md` | RESERVED/REDIS_APPLIED/COMMITTED 확인 |
| 2026-07-16 | Issue #119 ranking common ledger | Level 4 | PASS | Testcontainers Kafka·Redis·MySQL 양방향 복구, conflict, WRONGTYPE retry | `commands.md` | marker-only 방지, DLT↔Rebuild score 1 |
| 2026-07-16 | Issue #119 ranking common ledger | Level 5 | PASS | 실제 Compose normal, DLT→Rebuild, Rebuild→DLT, pending guard | `manual-qa.md` | score 1/1, lag 0, lock cleanup |

Level 5 required: YES, Level 6 required: NO. 독립 Review·QA와 최신 CI는 Ready PR에서 후속 확인합니다.
