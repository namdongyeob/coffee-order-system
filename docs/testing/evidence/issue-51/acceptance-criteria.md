# Issue #51 Acceptance Criteria

Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/51

Execution mode: STRICT
Execution mode reason: harness 검사 코드와 evidence 구조를 변경하므로 독립 Review, QA, CI가 필요합니다.
Level 5 required: NO
Level 5 reason: 애플리케이션 런타임 변경이 없습니다.
Level 6 required: NO
Level 6 reason: HTTP 계약 변경이 없습니다.

## 완료 기준

- PASS. 서로 다른 Issue의 verification fixture가 공통 정본 파일을 수정하지 않습니다. Dev 전체 harness 94건 PASS와 독립 QA harness gate PASS로 확인했습니다.
- PASS. Level 형식과 필수 PASS 교차 검사가 Issue별 정본에서도 유지됩니다. Dev focused 19건 PASS와 독립 QA focused 21건 PASS로 확인했습니다.
- PASS. 기존 전역 행을 원문 보존 이관하고 전역 뷰 재현 명령을 문서화했습니다. 독립 QA에서 `base_rows=89`, `rebuilt_rows=90`, `missing=0`, rebuild PASS를 확인했습니다.

Review는 head `b98b02e9c89b2d5f6a213de285338fcd7332e1f1`에서 `APPROVED`, 독립 QA는 같은 head에서 `PASS`입니다. 두 역할의 수행 시각은 당시 기록되지 않아 `미측정`입니다. CI 상태는 GitHub 정본에서 별도로 확인하며 이 문서의 완료 기준 판정에 복제하지 않습니다.
