# Issue #12 Acceptance Criteria

Issue: #12
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/12
Branch: codex/issue-12-http-artifacts

Execution mode: STRICT
Execution mode reason: 실제 local runtime에서 성공·실패 HTTP 계약을 Level 6으로 검증하고 독립 Review, QA, Docs와 CI를 거치는 고정 자율 큐 Issue입니다.
Level 5 required: YES
Level 5 reason: 실제 HTTP 요청 전에 MySQL·Redis·Kafka와 local profile 애플리케이션이 준비됐는지 확인해야 합니다.
Level 6 required: YES
Level 6 reason: Issue 완료 기준이 구현된 API를 실제 요청 산출물로 검증하고 요청·응답 원문을 기록하는 것입니다.

## 완료 기준

- `http/issue-12-api-validation.http`가 메뉴 조회, 포인트 충전, 주문 결제, 인기 메뉴 Top 3를 재현합니다.
- 잔액 부족 주문과 없는 메뉴 주문 실패를 공통 에러 응답으로 재현합니다.
- clean project Compose와 local profile에서 고정 QA ID `1201`~`1203`을 사용합니다.
- 실제 Level 6 요청 URL, body, status, response body, 실행 시각과 profile을 원문으로 남깁니다.
- 프로젝트 Compose와 애플리케이션을 종료하고 범위 밖 컨테이너를 건드리지 않습니다.
- production, test, schema와 API 계약은 변경하지 않습니다.
