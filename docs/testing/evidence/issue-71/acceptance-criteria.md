# Issue #71 Acceptance Criteria

Issue: #71
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/71

Execution mode: STRICT
Execution mode reason: harness와 자율 큐 workflow policy를 경량화하는 P1 복구이므로 단일 Dev, fresh Review, independent QA, Docs evidence와 최신 CI가 필요합니다.

Level 5 required: NO
Level 5 reason: workflow 문서와 harness 계약만 변경하며 애플리케이션 runtime 동작을 변경하지 않습니다.

Level 6 required: NO
Level 6 reason: HTTP/API 또는 UI 수동 관찰이 대상이 아닙니다.

## 완료 조건

- #66 metadata recovery budget, pre-review 미래 역할 링크 의존, 다중 가변 snapshot 동기화를 제거합니다.
- #60 고정 자율 큐와 조건부 merge, #55 경량 한국어 PR body, 저장소 밖 UTF-8 no-BOM preflight, 실제 Attempt 시각 기록을 보존합니다.
- Attempt, 명령, GitHub 가변 상태, 파생 metrics, 최종 repository 검증의 단일 정본을 분리합니다.
- QA 이후 고정 Markdown evidence 5개와 `docs/testing/verification-log.md`만 바뀐 경우에만 QA를 유지하고 final Review가 delta를 검토합니다.
- 최소 10개 행위 계약, 전체 harness, Issue repository gate, `git diff --check`를 통과합니다.
- Docs evidence는 한 번만 최종 동기화하며 이후 Review, QA, CI, head, merge 상태를 repository snapshot으로 다시 기록하지 않습니다.
