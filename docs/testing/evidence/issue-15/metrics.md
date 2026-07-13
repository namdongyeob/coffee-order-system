# Issue Metrics

Issue: #15
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/15
Branch: codex/issue-15-dlt-replay
Measured at: 2026-07-13

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 1 | 미측정 | 0 | 1 | 0 | 0 | 0 | 10 |

## 측정 근거

- 작업 종료 시각을 기록하지 않아 작업 시간은 추정하지 않고 `미측정`으로 기록합니다.
- Agent 수는 현재까지 실제 수행한 Dev 역할 1명입니다. Main Coordinator와 CI는 제외합니다.
- 정체 1건은 정책 미결정과 Docker daemon 부재로 구현·검증을 안전 정지한 BLOCKED 판정입니다.
- Review와 QA는 BLOCKED 상태에서 실행하지 않았으므로 결함 수는 0으로 기록합니다.
- 읽은 핵심 문서 10개는 GitHub Issue #15, `AGENTS.md`, Context Router, Kafka 이벤트 흐름, Kafka 선택 ADR, Kafka replay ADR, recovery strategy, 운영 runbook, test strategy, evidence guide입니다.

## Evidence links

- Commands: `commands.md`
- Attempts: `attempt-log.md`
- Manual QA: `manual-qa.md`
