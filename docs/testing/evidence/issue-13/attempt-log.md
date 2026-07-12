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
