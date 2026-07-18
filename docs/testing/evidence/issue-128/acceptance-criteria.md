# Issue #128 Acceptance Criteria

- [x] `BLOCKED` disposition은 required Level의 완료 `PASS`를 요구하지 않습니다.
- [x] `BLOCKED` disposition은 required Level마다 `PARTIAL` 행을 요구합니다.
- [x] `BLOCKED` disposition은 최신 `Failure Cause`에 정확한 blocker를 요구합니다.
- [x] `PASS` disposition의 required Level `PASS` fail-closed 검사를 유지합니다.
- [x] focused harness test, 전체 harness suite와 PR body preflight를 통과합니다.

Execution mode: STRICT
Execution mode reason: evidence reconciliation과 완료 gate 의미를 변경하는 workflow policy 작업입니다.
Level 5 required: NO
Level 5 reason: 하네스 정적 검증 계약만 변경하며 애플리케이션을 기동하지 않습니다.
Level 6 required: NO
Level 6 reason: HTTP API 동작을 변경하지 않습니다.
