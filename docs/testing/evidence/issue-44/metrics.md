# Issue Metrics

Issue: #44
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/44
Branch: codex/issue-44-harness-self-report-gates
Measured at: 2026-07-12

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 4 | 미측정 | 0 | 0 | 0 | 0 | 0 | 4 |

## 측정 근거

- 작업 시간은 최초 Generate부터 마지막 Reverification까지의 경과 시간입니다. Dev가 정확한 시작·종료 시각을 제공하지 않아 추정하지 않고 `미측정`으로 기록합니다.
- Agent 수 4는 Dev, Docs, Review, QA 역할입니다. Main Coordinator는 작업 Agent 수에 포함하지 않습니다.
- Review와 QA는 이 기록 시점에 pending이므로 결함 수는 현재 확정 결함 0이며, pending 상태 자체는 결함으로 산정하지 않습니다.
- 재시도와 블로커는 0입니다. TDD RED는 구현 전 계약 부재 확인이며 수정 반환 Attempt가 아닙니다.
- 직접 문서 4개는 `docs/ai/context-router.md`, `docs/ai/orchestration-policy.md`, `docs/testing/test-strategy.md`, `docs/testing/evidence-guide.md`입니다.

## Evidence links

- Commands: `commands.md`.
- Attempts: `attempt-log.md`.
- Manual QA: `manual-qa.md`.
