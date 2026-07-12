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

- 없음.
