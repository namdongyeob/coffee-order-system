# 수동 검토

- Issue #29 본문의 Legacy 인정과 backfill 조건을 `e5b47bd` 이전 경계 및 현재 Issue evidence 작성 규칙과 대조합니다.
- 실제 열린 GitHub Issue #29 URL, branch `codex/issue-29-harness-baseline`, evidence 경로 `issue-29`와 각 evidence 본문의 번호를 대조합니다.
- `agent-mistakes.md`의 PR #31 실제 실패 기록과 새 기록 조건을 대조해 추측·일시 오류의 중복 기록을 금지하는지 확인합니다.
- metrics template의 위치, 고정 표 열, source 링크를 확인합니다.
- 하네스 변경 예외가 실제 실패, 지표, 보안, CI·도구 호환성, 명시 요구사항으로 제한되고 Agent merge·close 금지가 사람 승인 경계와 함께 유지되는지 확인합니다.

API, DB, Kafka, Redis와 로컬 애플리케이션은 변경하지 않았으므로 Level 5와 Level 6 수동 검증은 수행 대상이 아닙니다.
