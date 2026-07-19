# 검증 로그

Attempt: 1
Head: edf3984688fbfe5efb9a9e3753da48f71dfbf08e

| 날짜 | Issue | Level | 결과 | 검증 범위 | 명령/Evidence | 비고 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-19 | Issue #132 ranking rebuild late-join fence | Level 0 | PASS | scope, diff, 금지 범위, cleanup | `commands.md`, `manual-qa.md` | 범위 밖 변경·잔여 인프라 0 |
| 2026-07-19 | Issue #132 ranking rebuild late-join fence | Level 1 | PASS | fence 분기, consumer source/header, focused regression | `commands.md` | 전체 Level 1 미실행, focused 합계 50 tests |
| 2026-07-19 | Issue #132 ranking rebuild late-join fence | Level 3 | PASS | MySQL ledger state/source, marker·score, retry와 rebuild run 취소/완료 | `commands.md`, `manual-qa.md` | COMMITTED/score 1 정합 |
| 2026-07-19 | Issue #132 ranking rebuild late-join fence | Level 4 | PASS | 실제 Testcontainers Kafka offset·Redis lock/swap·consumer interleaving | `RankingRebuildLateJoinIntegrationTest`, `commands.md` | 3 ordering tests, DLT/bilateral 회귀 포함 |
| 2026-07-19 | Issue #132 ranking rebuild late-join fence | Level 5 | PASS | 실제 Compose DB·Kafka·Redis + normal/rebuild 별도 jar 프로세스 | `manual-qa.md` | fenced mutation 0, release 후 score 1, active fail-closed, rebuild COMPLETED |

Level 5 required: YES, Level 6 required: NO. Level 6은 HTTP API 계약 변경이 없어 실행하지 않았습니다. 독립 Review·QA와 최신 PR-head CI는 후속 확인합니다.
