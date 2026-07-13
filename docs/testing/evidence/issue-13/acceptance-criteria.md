# Issue #13 Acceptance Criteria

Issue: #13
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/13

Execution mode: STRICT
Execution mode reason: performance와 Level 7 실제 부하 관찰이므로 독립 Review, QA, Docs, CI가 모두 필요합니다.
Level 5 required: YES
Level 5 reason: clean Compose와 local profile 애플리케이션 기동이 실제 k6 실행의 전제입니다.
Level 6 required: YES
Level 6 reason: k6가 실제 주문 HTTP 요청을 보내고 성공·오류 응답을 분류해야 합니다.

## 완료 기준

- [x] 주문 API Load, Stress, Spike 시나리오가 각각 존재합니다.
- [x] synthetic 사용자 포인트 준비 조건과 재현 절차가 문서화됩니다.
- [x] safe 기본값과 명시적 heavy opt-in을 구분합니다.
- [x] status·content type·실제 JSON parse·필수 주문 필드 성공 분류가 actual `createOrder()`의 성공·오류 Rate 기록까지 연결됨을 검증하고 p95, RPS, tag와 threshold를 기록합니다.
- [x] 세 스크립트의 contract test와 `k6 inspect`가 통과합니다.
- [x] clean local 환경에서 safe Level 7 세 시나리오를 실제 실행하고 원문·summary·환경·cleanup을 남깁니다.
- [x] k6 결과를 정합성 증명, 성능 튜닝 확정 또는 인프라 증설 근거로 과장하지 않습니다.

## 제외 범위

- production 동작, API 계약, build, runtime 설정 변경.
- 성능 튜닝 확정, 인프라 증설, 최대 처리량 확정.
- k6만으로 포인트·주문 정합성 증명.

## 최종 승인 상태

- 마지막 test-only remediation은 사용자 별도 승인 범위에서 필수 필드 누락 응답을 actual `runCreateOrder()` → `createOrder()` 경로로 검증하도록 변경했습니다.
- actual k6 contract는 assertions 7/7로 PASS했고 focused Python 3건, 세 스크립트 `k6 inspect`, repository gate, diff check, live 한국어 PR body preflight가 모두 PASS했습니다.
- invalid-response 내부 check 실패 5건은 malformed·HTTP 오류·non-JSON·필수 필드 누락 응답이 거부됐음을 관찰하는 의도된 실패이며 `contract_assertions` threshold 실패가 아닙니다.
- 마지막 test-only 변경은 runtime/helper를 바꾸지 않았으므로 직전 current-code Level 7 safe Load·Stress·Spike 결과와 cleanup evidence를 승계합니다. Level 7은 마지막 변경 뒤 재실행하지 않았습니다.
- fresh final Review는 P0/P1/P2 0건으로 `APPROVED`, independent QA는 `PASS`였습니다.
