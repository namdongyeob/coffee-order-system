# Issue #51 Acceptance Criteria

Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/51

Execution mode: STRICT
Execution mode reason: harness 검사 코드와 evidence 구조를 변경하므로 독립 Review, QA, CI가 필요합니다.
Level 5 required: NO
Level 5 reason: 애플리케이션 런타임 변경이 없습니다.
Level 6 required: NO
Level 6 reason: HTTP 계약 변경이 없습니다.

## 완료 기준

- 서로 다른 Issue의 verification fixture가 공통 정본 파일을 수정하지 않습니다.
- Level 형식과 필수 PASS 교차 검사가 Issue별 정본에서도 유지됩니다.
- 기존 전역 행을 원문 보존 이관하고 전역 뷰 재현 명령을 문서화합니다.
