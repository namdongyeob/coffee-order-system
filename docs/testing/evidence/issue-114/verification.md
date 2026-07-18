# 검증 로그

Attempt: 1
Head: e9412ab3cc4ceb56de5b4ae9659a0e9e3a5d59ec

| 날짜 | Issue | Level | 결과 | 검증 범위 | 명령/Evidence | 비고 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-18 | Issue #114 final runtime verification | Level 0 | PASS | clean exact head, 금지 범위, evidence, cleanup receipt | `commands.md`, `manual-qa.md` | runtime 전후 사용자 변경 0, cleanup orphan 0 |
| 2026-07-18 | Issue #114 final runtime verification | Level 3 | PASS | 실제 MySQL 주문·포인트·Outbox·processed·ranking ledger 정합성 | `manual-qa.md` | 최종 517/517/517/517, unpublished 0 |
| 2026-07-18 | Issue #114 final runtime verification | Level 4 | PASS | 실제 Kafka event·consumer lag과 Redis ranking 변화 | `manual-qa.md` | Kafka 517/517 lag 0, Redis score 517 |
| 2026-07-18 | Issue #114 final runtime verification | Level 5 | PASS | compose health, Flyway V7, local app startup와 health | `commands.md` | 3 services healthy, HTTP health 200 UP |
| 2026-07-18 | Issue #114 final runtime verification | Level 6 | PASS | 메뉴·충전·주문결제·Top3와 invalid amount·insufficient balance | `manual-qa.md` | 200/200/201/200, 실패 400/409 |
| 2026-07-18 | Issue #114 final runtime verification | Level 7 | PASS | k6 safe Load·Stress·Spike | `commands.md` | p95 60.45/59.74/72.04ms, errors 0%, checks 100% |

Level 5 required: YES, Level 6 required: YES. 두 Level 모두 exact production head에서 PASS했습니다. 전체 Gradle Level 1 회귀는 실행하지 않았으며 최신 evidence-only PR head GitHub Actions `quality-gates`가 소유합니다. fresh Review, independent QA와 CI는 draft PR 뒤 후속 확인합니다.
