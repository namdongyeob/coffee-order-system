# Issue #114 Acceptance Criteria

Issue: #114
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/114

Execution mode: STRICT
Execution mode reason: Docker 인프라, 실제 애플리케이션, HTTP, Kafka·Redis 관찰과 k6 성능 검증을 함께 수행하는 최종 runtime 검증 작업입니다.
Level 5 required: YES
Level 5 reason: 최종 제출 production head에서 실제 애플리케이션 기동 성공을 증명하는 것이 이 Issue의 핵심입니다.
Level 6 required: YES
Level 6 reason: 필수 API 4종과 핵심 실패 응답을 실제 HTTP 요청으로 검증하는 것이 이 Issue의 핵심입니다.

## Dev 완료 기준

- [x] clean 상태의 production head `e9412ab3cc4ceb56de5b4ae9659a0e9e3a5d59ec`에서 검증을 시작했습니다.
- [x] MySQL 8.4.5, Redis 7.4.2, Kafka 3.9.1 compose가 모두 `running/healthy`였습니다.
- [x] 사용자가 역사적 `exit 255` 원인 확인 AC를 이번 clean 실행의 비재현, compose health, restart 0, exit 0, OOM false 관찰로 대체했습니다. 결정 근거는 [Issue #114 comment 5010437517](https://github.com/namdongyeob/coffee-order-system/issues/114#issuecomment-5010437517)입니다.
- [x] local profile 애플리케이션이 기동되고 actuator health가 HTTP 200 `UP`을 반환했습니다.
- [x] 메뉴 목록, 포인트 충전, 주문·결제, 최근 7일 인기 메뉴 Top 3를 실제 HTTP로 확인했습니다.
- [x] 충전액 0은 HTTP 400 `INVALID_CHARGE_AMOUNT`, 1P 잔액 주문은 HTTP 409 `INSUFFICIENT_POINT`를 반환했습니다.
- [x] 주문 뒤 DB 주문·포인트·Outbox, Kafka 처리, Redis 랭킹 변화가 같은 이벤트와 수치로 일치했습니다.
- [x] k6 safe Load·Stress·Spike가 모두 exit 0이고 모든 threshold를 통과했습니다.
- [x] head, 명령, threshold, p95, error rate, checks를 Issue evidence 6종에 기록했습니다.
- [x] 앱과 이 검증이 만든 compose container·network·volume을 정리하고 orphan 0건을 확인했습니다.
- [x] production, test, runtime 설정, workflow와 검증 스크립트를 수정하지 않았습니다.
- [x] PR head `4f99e3bdd28ae2b6956ea35493d6658afae35018`의 GitHub Actions `quality-gates` runs `29636123309`, `29636123353`이 모두 SUCCESS였습니다.

## Current disposition

PASS. 사용자 결정에 따라 현재 clean 실행의 non-reproduction evidence가 역사적 root-cause AC를 대체합니다. 실제 runtime 결과는 Attempt 1에서 관찰됐고 Attempt 3에서는 production/runtime/k6를 재실행하지 않았습니다. independent QA와 최신 evidence-only PR-head CI는 후속 gate입니다.
