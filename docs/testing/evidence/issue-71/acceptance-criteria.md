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

- #66 metadata recovery budget과 pre-review 미래 역할 링크 의존성을 제거합니다.
- #60 고정 자율 큐와 조건부 merge, #55 경량 PR body·UTF-8 no-BOM preflight·Attempt 시각 기록을 유지합니다.
- 단일 정본과 Review → QA → Docs 1회 → final Review → CI 순서를 문서화합니다.
- QA 뒤 Issue evidence allowlist docs-only delta는 QA를 유지하고 그 밖의 delta는 stale로 판정합니다.
- 최소 10개 행위 계약 테스트와 전체 harness, repository gate, diff check를 통과합니다.
