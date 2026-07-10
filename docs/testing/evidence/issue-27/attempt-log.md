# Issue Attempt Log

Issue: #27
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/27
Branch: codex/issue-27-review-qa-gates

## Attempt 1

### Generate

- Review Gate를 신설하고 AI Slop 기준을 통합했습니다.
- QA Gate, Dev Agent 구현 가드레일, 역할별 핸드오프 계약, PR/Evidence 완료 주장 기준을 정리했습니다.
- 완료된 구현 계획을 archive로 이동하고 #27 evidence를 추가했습니다.

### Evaluate

- Level 0 문서 링크, 삭제·이동 파일 참조, hot path, 범위, diff 검사를 실행해 PASS했습니다.

### Failure Cause

- 없음.

### Change Scope

- `docs/ai/`, `.github/PULL_REQUEST_TEMPLATE.md`, `docs/testing/evidence/issue-27/`, `docs/testing/verification-log.md`의 #27 문서 정리만 허용합니다.

### Reverification

- `commands.md`의 명령을 실행하고 결과를 기록했습니다.

### Next Attempt

- 없음.

## Attempt 2

### Generate

- Claude 리뷰에서 누락으로 지적한 Review Router의 반복 실수와 계층 설계 정책을 필수 문서에 복원했습니다.
- Review Gate에 DLT의 테스트·검증 가능성과 동작 변경 문서 갱신 검토를 추가했습니다.
- 규칙 정본 지도에 QA 판정 기준 행을 추가했습니다.

### Evaluate

- Level 0 링크, 삭제·이동 참조, Review hot path 5개, diff 검사를 재실행해 PASS했습니다.

### Failure Cause

- 최초 Review hot path가 반복 실수와 계층 설계 정책을 읽지 않아 기존 Review 흐름의 검토 범위를 축소했습니다.

### Change Scope

- 리뷰 지적을 반영한 `context-router.md`, `review-gate.md`, `rule-source-map.md`와 #27 evidence, verification log만 허용합니다.

### Reverification

- `harness_gate` 링크 검사, 삭제·이동 참조 검색, 고정 문자열 기반 Review hot path 링크 수 확인, `git diff --check`을 실행해 PASS했습니다.

### Next Attempt

- 없음.
