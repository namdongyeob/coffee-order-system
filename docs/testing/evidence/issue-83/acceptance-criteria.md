# Issue #83 Acceptance Criteria

- [x] terminal `BLOCKED` evidence와 PASS verification 조합을 harness가 FAIL합니다.
- [x] PASS Attempt·verification·metrics 일치 fixture를 PASS합니다.
- [x] retry count와 current Attempt/head 불일치를 harness가 FAIL합니다.
- [x] #15형 metadata mismatch를 fresh Review 전에 자동 발견합니다.

Execution mode: STRICT
Execution mode reason: evidence contract와 harness preflight 의미를 변경합니다.
Level 5 required: NO
Level 5 reason: 애플리케이션 runtime을 변경하지 않습니다.
Level 6 required: NO
Level 6 reason: HTTP API 계약을 변경하지 않습니다.
