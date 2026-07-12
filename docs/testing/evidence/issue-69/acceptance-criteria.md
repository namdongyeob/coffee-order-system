# Issue #69 Acceptance Criteria

Issue: #69
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/69
Branch: codex/issue-69-gate-state-machine

Execution mode: STRICT
Execution mode reason: 자율 큐의 Review·QA·Docs·CI Gate와 harness/workflow policy를 변경하는 P1 복구 작업입니다.
Level 5 required: NO
Level 5 reason: workflow 상태 머신과 계약 테스트가 대상이며 애플리케이션 런타임을 변경하지 않습니다.
Level 6 required: NO
Level 6 reason: 실제 HTTP 또는 브라우저 관찰이 아니라 기계적 lifecycle 전이를 검증합니다.

## 완료 기준

- [x] 11개 상태와 현재·이전 단계 입력만 사용하는 전이 규칙을 단일 정책 정본에 반영합니다.
- [x] `PRE_REVIEW_READY`에서 미래 Review·QA 링크를 요구하지 않습니다.
- [x] GitHub 역할 댓글과 현재 head checks를 가변 상태의 정본으로 사용합니다.
- [x] clean PR이 0개 역할 댓글에서 `MERGE_READY`, merge·close와 다음 Issue 진행까지 순환 없이 전이하는 실행 가능한 계약 테스트를 추가합니다.
- [x] 미래 입력, 순환 의존, 역할 링크 누락, final Review SHA 불일치, PR snapshot 오판정 실패 테스트를 추가합니다.
- [x] 실제 GitHub-shaped snapshot을 읽는 harness CLI가 현재 Gate와 다음 허용 전이를 출력합니다.
- [x] head 변경 시 이전 initial Review·QA를 모두 stale로 판정하고 새 head의 fresh 결과 전에는 final Gate를 금지합니다.
- [ ] fresh read-only Review, independent QA, Docs final sync와 최신 CI를 완료합니다.

## 제외 범위 확인

- PR #68과 Issue #11의 production·test·Kafka 구현을 변경하지 않습니다.
- #11 Review·QA·Docs·merge와 #21 구현을 시작하지 않습니다.
- GitHub 저장소 설정과 P2 Review 정책을 변경하지 않습니다.
