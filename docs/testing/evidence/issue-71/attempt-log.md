# Issue #71 Attempt Log

Issue: #71
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/71
Branch: codex/issue-71-workflow-rollback

## Attempt 1

### Generate

- 역할: Dev
- 시작 시각: 2026-07-12T16:55:28.2765399+09:00
- 기준 head: `a6fac5d6bf9f72b986361065765d34524d5047a2`
- #66의 metadata recovery budget, pre-review 미래 링크 의존성과 문자열 계약을 제거하고 경량 workflow 행위 계약을 작성했습니다.

### Evaluate

- TDD RED: 신규 helper가 없는 상태에서 focused suite가 26 tests 중 신규 helper 부재 14 errors로 실패했습니다.
- GREEN: 행위 계약 focused 27 tests와 전체 harness 75 tests가 PASS했습니다.

### Failure Cause

- 경량 pre-review, 역할 수, QA head 유효성, merge·next Issue 판정 helper가 아직 구현되지 않았습니다.

### Change Scope

- #66 직접 연결 policy·Skill·agent rules·evidence guide, harness helper와 계약 테스트, Issue #71 evidence만 변경합니다.

### Reverification

- 종료 시각: 2026-07-12T17:01:07.9032630+09:00
- `python -m unittest scripts.tests.test_harness_gate.OrchestrationContractTest`: 27 tests PASS.
- `python -m unittest scripts.tests.test_harness_gate`: 75 tests PASS.
- Issue #71 repository gate와 `git diff --check`: PASS.

### Next Attempt

- fresh Review와 independent QA를 위한 draft PR 생성.
