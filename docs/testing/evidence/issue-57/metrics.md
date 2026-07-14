# Issue Metrics

Issue: #57
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/57
Branch: claude/issue-57-level-mapping-replay
Measured at: 2026-07-14

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 3 | 미측정 | 0 | 0 | 미확정 | 미확정 | 0 | 6 |

## 측정 근거

- 시작 시각이 기록되지 않아 작업 시간은 `미측정`입니다.
- STRICT 기본 구성은 Dev, Review, QA의 고유 역할 수 3입니다. Main Coordinator는 제외합니다. Docs Agent는 dispatch하지 않았으므로 4로 올리지 않습니다.
- 재시도 0은 Current Attempt 1 - 1입니다.
- Review·QA 결함 수는 fresh subagent 결과 반영 뒤 갱신합니다.
- 읽은 핵심 문서는 `AGENTS.md`, `.codex/skills/coffee-order-issue-loop/SKILL.md`, `docs/ai/context-router.md`, `docs/ai/orchestration-policy.md`, `docs/ai/agent-rules.md`, `docs/testing/test-strategy.md`, `docs/testing/evidence-guide.md` 중 하네스·스크립트 hot path 필수 4개(orchestration-policy, agent-rules, test-strategy, evidence-guide) + 공통 진입 2개(AGENTS.md, context-router.md) = 6개입니다.
