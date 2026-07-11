# Issue Metrics

Issue: #40
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/40
Branch: codex/issue-40-kafka-consumer-idempotency
Measured at: 2026-07-11

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 4 | 15 | 0 | 0 | 0 | 0 | 0 | 11 |

## 측정 근거

- Dev Attempt 1은 `2026-07-11T13:54:26.475+09:00`부터 `2026-07-11T14:09:08.832+09:00`까지 정확히 `882.357s`, 즉 `14.70595분`입니다. 고정 template의 정수 분 형식에 따라 최근접 정수 `15`로 기록했습니다.
- Agent 수 4는 STRICT mode의 Dev, Review, QA, Docs 역할입니다. Review, QA, Docs는 아직 pending입니다.
- TDD RED와 Level 4 RED는 계획된 검증 단계이므로 재시도나 정체로 세지 않았습니다.
- Review/QA 전이므로 결함 수는 현재 0이며 독립 보고 뒤 Docs가 확정합니다.
- 읽은 핵심 문서는 AGENTS/issue-loop, Kafka hot path 5개, ERD, orchestration, agent rules, test strategy, evidence guide입니다.

## Evidence links

- Commands: `commands.md`.
- Attempts: `attempt-log.md`.
- Manual QA and atomicity limit: `manual-qa.md`.
