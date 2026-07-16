# 검증 로그

Attempt: 1
Head: 45b3a3f8686e2e469e029d6bb0846c8910bcfc28

| 날짜 | Issue | Level | 결과 | 검증 범위 | 명령/Evidence | 비고 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-16 | Issue #119 ranking common ledger | Level 0 | PASS | scope, diff, fingerprint fail-closed, secret·large-file | `commands.md`, `attempt-log.md` | #112 Rebuild bulk 동작 변경 없음 |
| 2026-07-16 | Issue #119 ranking common ledger | Level 1 | PASS | 관련 clean 75, 전체 clean 125 | `commands.md` | failures/errors/skipped 0 |
| 2026-07-16 | Issue #119 ranking common ledger | Level 3 | PASS | MySQL ledger durable states와 retry, schema V6 | `commands.md` | RESERVED/REDIS_APPLIED/COMMITTED 확인 |
| 2026-07-16 | Issue #119 ranking common ledger | Level 4 | PASS | Testcontainers Kafka·Redis·MySQL 양방향 복구와 conflict | `commands.md` | DLT↔Rebuild score 1 |
| 2026-07-16 | Issue #119 ranking common ledger | Level 5 | PASS | 실제 Compose normal, DLT→Rebuild, Rebuild→DLT, pending guard | `manual-qa.md` | score 1/1, lag 0, lock cleanup |

Level 5 required: YES, Level 6 required: NO. 독립 Review·QA와 최신 CI는 Ready PR에서 후속 확인합니다.
