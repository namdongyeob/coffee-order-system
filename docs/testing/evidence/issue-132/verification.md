# 검증 로그

Attempt: 2
Head: d5f5bb55d236350c7055d6f69b5611cdd24a956a

| 날짜 | Issue | Level | 결과 | 검증 범위 | 명령/Evidence | 비고 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-19 | Issue #132 ranking rebuild late-join fence | Level 0 | PASS | scope, diff, 금지 범위, cleanup | `commands.md`, `manual-qa.md` | 범위 밖 변경·잔여 인프라 0 |
| 2026-07-19 | Issue #132 ranking rebuild late-join fence | Level 1 | PASS | fence 분기, consumer source/header, focused regression | `commands.md` | 전체 Level 1 미실행, focused 합계 50 tests |
| 2026-07-19 | Issue #132 ranking rebuild late-join fence | Level 3 | PASS | MySQL ledger state/source, marker·score, retry와 rebuild run 취소/완료 | `commands.md`, `manual-qa.md` | COMMITTED/score 1 정합 |
| 2026-07-19 | Issue #132 ranking rebuild late-join fence | Level 4 | PASS | 실제 Testcontainers Kafka offset·Redis lock/swap·consumer interleaving | `RankingRebuildLateJoinIntegrationTest`, `commands.md` | 3 ordering tests, DLT/bilateral 회귀 포함 |
| 2026-07-19 | Issue #132 ranking rebuild late-join fence | Level 5 | PASS | 실제 Compose DB·Kafka·Redis + normal/rebuild 별도 jar 프로세스 | `manual-qa.md` | fenced mutation 0, release 후 score 1, active fail-closed, rebuild COMPLETED |
| 2026-07-19 | Issue #132 Attempt 2 owner/run observability | Level 1 | PASS | structured owner 예외·consumer log와 기존 source/header | `commands.md` | focused unit 13 tests |
| 2026-07-19 | Issue #132 Attempt 2 deterministic ordering | Level 3 | PASS | release 전 ledger/marker/score, 최종 ledger source/state | `commands.md` | final focused 52 tests |
| 2026-07-19 | Issue #132 Attempt 2 deterministic ordering | Level 4 | PASS | 실제 processor fence attempt latch, uncommitted offset, DLT 0, consumer-first | `RankingRebuildLateJoinIntegrationTest`, `commands.md` | 4 ordering tests, arbitrary long sleep 제거 |
| 2026-07-19 | Issue #132 Attempt 2 owner/run observability | Level 5 | PASS | actual consumer-first·rebuild-first 별도 jar와 DB·Kafka·Redis | `manual-qa.md` | 동일 owner/run 로그, 최종 score 1·lag 0·DLT 0 |

Level 5 required: YES, Level 6 required: NO. Level 6은 HTTP API 계약 변경이 없어 실행하지 않았습니다. 고정 head QA PASS는 Attempt 2 production 변경으로 stale이며 최신 Review·CI는 후속 확인합니다.
