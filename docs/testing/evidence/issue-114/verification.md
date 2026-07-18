# 검증 로그

Attempt: 2
Head: 5aedd45dbc3d0fea25757ae13f18f0084853a653

| 날짜 | Issue | Level | 결과 | 검증 범위 | 명령/Evidence | 비고 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-18 | Issue #114 final runtime verification | Level 0 | PARTIAL | clean exact production head, 금지 범위, evidence, cleanup receipt, current-head CI | `commands.md`, `manual-qa.md` | CI SUCCESS·cleanup 0이나 exit 255 원인 evidence 누락 |
| 2026-07-18 | Issue #114 final runtime verification | Level 3 | PARTIAL | 실제 MySQL 주문·포인트·Outbox·processed·ranking ledger 정합성 | `commands.md`, `manual-qa.md` | 관찰은 517/517/517/517, unpublished 0이나 Issue 전체 BLOCKED |
| 2026-07-18 | Issue #114 final runtime verification | Level 4 | PARTIAL | 실제 Kafka event·consumer lag과 Redis ranking 변화 | `commands.md`, `manual-qa.md` | 관찰은 Kafka 517/517 lag 0, Redis score 517이나 Issue 전체 BLOCKED |
| 2026-07-18 | Issue #114 final runtime verification | Level 5 | PARTIAL | compose health, Flyway V7, local app startup와 health, 과거 exit 255 원인 | `commands.md`, `manual-qa.md` | 현재 3 services healthy·HTTP 200 UP, 과거 exit 255 원인 미확인 |
| 2026-07-18 | Issue #114 final runtime verification | Level 6 | PARTIAL | 메뉴·충전·주문결제·Top3와 invalid amount·insufficient balance | `commands.md`, `manual-qa.md` | 관찰은 200/200/201/200·실패 400/409이나 Issue 전체 BLOCKED |
| 2026-07-18 | Issue #114 final runtime verification | Level 7 | PARTIAL | k6 safe Load·Stress·Spike | `commands.md` | 관찰은 p95 60.45/59.74/72.04ms·errors 0%·checks 100%이나 Issue 전체 BLOCKED |

Level 5 required: YES, Level 6 required: YES. Attempt 1의 현재 runtime 관찰은 성공했지만 Level 5 범위의 과거 container `exit 255` 원인 확인 근거가 없어 최종 판정은 BLOCKED입니다. PR head `5aedd45dbc3d0fea25757ae13f18f0084853a653`의 GitHub Actions `quality-gates` run `29635241238`은 SUCCESS이며 누락된 runtime artifact를 대체하지 않습니다. PR body preflight는 BLOCKED의 PASS 행 금지와 required Level 5/6 PASS 강제가 충돌해 FAIL했고 body edit은 보류했습니다. independent QA는 pending입니다.
