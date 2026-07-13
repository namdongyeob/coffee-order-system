# Issue Metrics

Issue: #36
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/36
Branch: claude/issue-36-doc-lifecycle-audit
Measured at: 2026-07-13

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STANDARD | 2 | 미측정 | 1 | 0 | 1 | 0 | 0 | 9 |

## 측정 근거

- 시작 시각이 기록되지 않아 작업 시간은 `미측정`입니다.
- STANDARD 기본 구성은 Dev, Combined Verifier의 고유 역할 수 2입니다. Main Coordinator는 제외합니다.
- 재시도 1은 fresh Combined Verifier의 `REVISE` 뒤 Attempt 2(head `f8756fb`)의 지적 1건 정정입니다.
- Review 결함(Combined Verifier 지적) 1은 `docs/adr/README.md`가 54개 인벤토리 분류에서 누락된 것입니다. 같은 정정 1회로 해소했습니다. QA 결함은 STANDARD에서 별도 QA 역할이 없어 0으로 둡니다.
- 읽은 핵심 문서는 `AGENTS.md`, `.codex/skills/coffee-order-issue-loop/SKILL.md`, `docs/ai/context-router.md`, `docs/ai/orchestration-policy.md`, `docs/ai/agent-rules.md`, `docs/ai/autonomous-queue-runbook.md`, `docs/testing/test-strategy.md`, `docs/testing/evidence-guide.md`, `docs/ai/rule-source-map.md` 9개입니다.
- CI는 push 뒤 GitHub 새 head에서 확인합니다.
