# Issue Metrics

Issue: #15
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/15
Branch: codex/issue-15-dlt-replay
Measured at: 2026-07-13

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 4 | 미측정 | 2 | 1 | 0 | 0 | 0 | 5 |

## 측정 근거

- Attempt 3의 시작·중단 시각은 기록되지 않아 전체 작업 시간은 추정하지 않고 `미측정`으로 기록합니다. Testcontainers MySQL context 재시도 정체는 3분 초과로만 관찰됐습니다.
- STRICT 기본 Agent 수는 Dev, Review, QA의 고유 역할 3이며, metadata 불일치 동기화를 위해 실제 dispatch한 Docs Agent를 포함해 4입니다. Main Coordinator와 CI는 제외합니다.
- 재시도 수 2는 Current Attempt 3에서 계산했습니다. Attempt 3의 전체 Gradle 명령이 terminal 결과 없이 중단돼 정체 수는 1입니다. Review·QA 결함은 아직 관찰되지 않아 0입니다.
- 읽은 핵심 문서는 GitHub Issue #15, `AGENTS.md`, `docs/ai/orchestration-policy.md`, `docs/ai/agent-rules.md`, `docs/testing/evidence-guide.md`입니다.

## Evidence links

- Commands: `commands.md`
- Attempts: `attempt-log.md`
- Manual QA: `manual-qa.md`
