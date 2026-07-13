# Issue #13 Attempt Log

Issue: #13
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/13
Branch: codex/issue-13-k6-scenarios

## Attempt 1

### Generate

- 시작: 2026-07-12T20:42:04.2408574+09:00.
- 먼저 존재하지 않는 세 script와 공통 helper의 contract test를 작성했습니다.
- Load, Stress, Spike script, synthetic data setup, safe/heavy profile, thresholds와 문서를 구현했습니다.

### Evaluate

- RED: `python -m unittest k6.tests.test_scenarios`는 script/helper 부재로 4 errors였습니다.
- 중간 FAIL: inspect JSON의 실제 schema와 공통 helper 위치에 맞지 않은 test assertion 2종을 확인했습니다.
- GREEN: contract 2 tests와 세 `k6 inspect`가 통과했습니다.
- Level 7 safe 실제 실행은 Load, Stress, Spike 모두 threshold PASS와 주문 오류율 0%였습니다.

### Failure Cause

- 초기 RED는 의도한 contract-first 상태였습니다.
- 중간 test는 `maxVUs`가 inspect root가 아니라 scenario stage target으로 표현되고 `K6_PROFILE` 정본이 공통 helper에 있는 구조를 반영하지 못했습니다.

### Change Scope

- `k6/` script·contract test와 Issue가 지정한 k6 계획·README·결과 문서, Issue #13 evidence·검증 로그만 변경했습니다.

### Reverification

- 종료: 2026-07-12T21:01:35.7800916+09:00.
- contract 2건, 세 `k6 inspect`, Issue #13 repository gate, 링크 검사, `git diff --check`, PR body preflight가 PASS했습니다.
- heavy profile의 포인트 소진이 성능 오류로 오인되지 않도록 profile별 충전 횟수를 명시한 뒤 current code로 safe 세 시나리오를 다시 실행했습니다.
- 실제 k6 실행과 cleanup 결과는 `commands.md`, `manual-qa.md`, `*-output.txt`, `*-summary.json`에 연결합니다.

### Next Attempt

- Fresh Review P1: malformed JSON 201 응답이 성공으로 분류되지 않도록 실제 JSON parse와 최소 필드 검증을 추가하고 actual k6 contract와 safe Level 7을 재검증합니다.

## Attempt 2 — 허용된 P1 remediation

### Generate

- 시작: 2026-07-12T21:07:37.3454887+09:00.
- `classifyOrderResponse`가 HTTP 201, JSON Content-Type, actual `response.json()` parse와 최소 주문 필드를 한 번에 판정하도록 추가했습니다.
- parse 예외는 catch해 `succeeded=false`, `bodyOk=false`로 반환하고 주문 error rate에 반영합니다.

### Evaluate

- RED: actual k6 contract가 missing export로 1 failure였습니다.
- GREEN: valid 응답 성공, malformed JSON과 필수 필드 누락 실패를 actual k6 runtime에서 검증했고 Python suite 3 tests가 PASS했습니다.
- current-code safe Load, Stress, Spike는 주문 47·185·271건, p95 179.33·117.95·69.35ms, 오류율 0%와 전체 threshold PASS였습니다.

### Failure Cause

- 초기 구현은 Content-Type 문자열만 검사해 malformed JSON body를 성공으로 분류할 수 있었습니다.

### Change Scope

- 공통 주문 응답 분류 helper, actual k6 contract test와 확정된 결과를 담는 Issue #13 문서·evidence만 변경했습니다. P2 unknown profile test는 수정하지 않았습니다.

### Reverification

- 종료: 2026-07-12T21:14:20.6724825+09:00.
- contract suite 3건, 세 `k6 inspect`, current-code safe Level 7, cleanup, repository gate, diff check와 PR body preflight를 재실행해 PASS했습니다.
- 첫 통합 명령에서 PowerShell의 공백 없는 `>$null`이 script 경로 일부로 전달된 명령 오타 1건은 `Out-Null`로 고쳐 같은 검증을 통과했으며 구현 결함이나 추가 Attempt가 아닙니다.

### Next Attempt

- 없음.

## Attempt 4 — 사람이 승인한 마지막 test-only remediation

### Generate

- 시작: 2026-07-13T09:24:12.9815154+09:00.
- Review가 missing-field 응답만 classifier 직접 호출에 남아 actual `createOrder()`와 Rate recorder 연결을 검증하지 않는 동일 P1을 확인했고, 이 Review evidence를 RED 재현 근거로 사용했습니다.
- 사용자가 승인한 정확한 test-only 범위에서 missing-field case를 기존 `runCreateOrder(missingFieldResponse)` 경로로 교체했습니다.

### Evaluate

- GREEN: actual k6 contract는 missing-field 응답에서 success recorder `[false]`, error recorder `[true]`, 각 length 1을 확인했습니다.
- Contract threshold `contract_assertions`는 7/7, actual k6 contract와 focused Python suite 3건이 PASS했습니다. Invalid 응답의 내부 check 실패 5건은 기대한 분류 관찰이며 contract threshold 실패가 아닙니다.

### Failure Cause

- 이전 contract의 missing-field case만 classifier 직접 호출에 남아 actual metric recording 연결을 증명하지 못했습니다.

### Change Scope

- `k6/tests/order-response-contract.js`의 missing-field case와 이 사실을 기록하는 허용 evidence·PR body만 변경했습니다. Runtime 동작은 변경하지 않았습니다.

### Reverification

- 종료: 2026-07-13T09:25:48.1342944+09:00.
- Actual k6 contract, focused Python suite 3건, 세 `k6 inspect`, repository gate, diff check와 PR body preflight가 PASS했습니다.

### Next Attempt

- 없음.

## Attempt 3 — 사람이 승인한 별도 제한 P1 remediation

### Generate

- 시작: 2026-07-13T09:03:34.2731185+09:00.
- 두 번째 Review P1 안전 정지 뒤 사용자가 actual `createOrder()`와 Rate 기록 연결만 검증하는 별도 제한 remediation을 승인했습니다. 일반 retry 정책이나 범위를 변경한 것이 아닙니다.
- production app이 아닌 k6 helper에 HTTP post, 성공·오류 Rate recorder와 sleep을 주입하는 최소 dependency seam을 추가했습니다. 기본 실행은 기존 실제 HTTP·Rate·sleep을 그대로 사용합니다.

### Evaluate

- RED: contract가 주입값을 무시한 기존 `createOrder()`를 실행해 localhost 실제 호출 4건, recorder 호출 0건이 되었고 19 checks 중 16건이 실패했습니다.
- GREEN: valid JSON은 success `true`/error `false`, malformed JSON·HTTP 500·non-JSON은 success `false`/error `true`를 각각 한 번 기록함을 actual k6 runtime에서 확인했습니다.
- current-code safe Load, Stress, Spike는 41·111·237 주문 iterations, p95 293.96·628.41·145.15ms, 오류율 0%와 전체 threshold PASS였습니다.

### Failure Cause

- 첫 P1 contract는 classifier 반환만 검증해 actual `createOrder()`와 Rate 기록 사이 연결이 끊겨도 통과할 수 있었습니다.

### Change Scope

- `k6/lib/order-scenario.js`, `k6/tests/order-response-contract.js`, focused Python assertion과 확정 결과를 담는 Issue #13 문서·evidence만 변경했습니다.

### Reverification

- 종료: 2026-07-13T09:09:48.2659447+09:00.
- actual k6 contract와 focused Python suite 3건, 세 `k6 inspect`, current-code safe Level 7, cleanup, repository gate, diff check와 PR body preflight를 재실행해 PASS했습니다.

### Next Attempt

- 없음.

## 최종 Docs 동기화

- actual k6 contract는 exit 0, assertions 7/7이었고 focused Python 3/3, 세 `k6 inspect`, repository gate, diff check, live 한국어 PR body 외부 UTF-8 no-BOM preflight가 PASS했습니다.
- invalid-response 내부 check 실패 5건은 의도된 거부 관찰이며 contract threshold는 PASS했습니다.
- 마지막 변경은 contract test와 evidence만 바꿨고 runtime/helper는 불변이므로 직전 current-code Level 7 결과와 cleanup evidence는 유효합니다. 마지막 test-only remediation 뒤 Level 7은 재실행하지 않았습니다.
- 최종 Docs 역할은 허용된 Issue #13 evidence 5개와 `docs/testing/verification-log.md`만 append하고 k6 script·test·raw result와 PR 본문을 변경하지 않습니다.

## Docs metadata-only recovery

- 변동 가능한 GitHub HEAD·Review·QA 상태를 저장소 evidence에 복제한 뒤 Docs 커밋으로 해당 상태가 stale해졌고, 사용자가 이 snapshot 표현의 제거를 승인했습니다.
- 실제 명령·테스트 결과·카운트·Level 7 지표·contract assertions·기대된 invalid-response 내부 check·파일 경로·기능 결정·남은 위험은 불변 근거로 유지합니다.
