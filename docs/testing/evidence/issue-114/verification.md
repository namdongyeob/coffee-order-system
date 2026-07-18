# 검증 로그

Attempt: 4
Head: 4f99e3bdd28ae2b6956ea35493d6658afae35018

| 날짜 | Issue | Level | 결과 | 검증 범위 | 명령/Evidence | 비고 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-18 | Issue #114 final runtime verification | Level 0 | PASS | clean exact production head, 금지 범위, evidence, cleanup receipt, 사용자 AC 결정 | `commands.md`, `manual-qa.md` | cleanup 0, user decision 5010437517 |
| 2026-07-18 | Issue #114 final runtime verification | Level 3 | PASS | 실제 MySQL 주문·포인트·Outbox·processed·ranking ledger 정합성 | `commands.md`, `manual-qa.md` | 최종 517/517/517/517, unpublished 0 |
| 2026-07-18 | Issue #114 final runtime verification | Level 4 | PASS | 실제 Kafka event·consumer lag과 Redis ranking 변화 | `commands.md`, `manual-qa.md` | Kafka 517/517 lag 0, Redis score 517 |
| 2026-07-18 | Issue #114 final runtime verification | Level 5 | PASS | exact inspect health/restart/exit/OOM, Flyway V7, local app startup와 actuator | `commands.md`, `manual-qa.md` | 3 services running/healthy, restart 0, exit 0, OOM false, HTTP 200 UP |
| 2026-07-18 | Issue #114 final runtime verification | Level 6 | PASS | 메뉴·충전·주문결제·Top3와 invalid amount·insufficient balance | `commands.md`, `manual-qa.md` | 200/200/201/200, 실패 400/409 |
| 2026-07-18 | Issue #114 final runtime verification | Level 7 | PASS | k6 safe Load·Stress·Spike | `commands.md` | p95 60.45/59.74/72.04ms, errors 0%, checks 100% |

Level 5 required: YES, Level 6 required: YES. 사용자가 [Issue #114 comment 5010437517](https://github.com/namdongyeob/coffee-order-system/issues/114#issuecomment-5010437517)에서 역사적 `exit 255` root-cause AC를 현재 clean 실행의 non-reproduction evidence로 대체해 최종 판정은 PASS입니다. exact `docker inspect`와 actuator 명령·출력은 `commands.md`에 있습니다. PR head `4f99e3bdd28ae2b6956ea35493d6658afae35018`의 runs `29636123309`, `29636123353`은 모두 SUCCESS입니다. Attempt 4 evidence-only head CI와 independent QA는 후속 확인합니다.
