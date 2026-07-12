# Issue Metrics

Issue: #71
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/71
Branch: codex/issue-71-workflow-rollback
Measured at: 2026-07-12

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 4 | 6 | 0 | 0 | 0 | 0 | 0 | 5 |

## 측정 근거

- Agent 수 4는 STRICT 고유 역할 Dev, Review, QA, Docs이며 Main Coordinator와 CI는 제외합니다. 동일 역할 재시도는 중복하지 않습니다.
- 작업 시간은 당시 기록한 최초 Generate `2026-07-12T16:55:28.2765399+09:00`부터 마지막 Reverification `2026-07-12T17:01:07.9032630+09:00`까지 5분 39.6267231초를 분 단위 정수 6으로 기록했습니다. 추정값이 아닙니다.
- 재시도·정체·Review·QA 결함과 범위 밖 변경은 현재 0입니다.
- 핵심 문서는 Issue #71, project Issue Loop Skill, orchestration policy, agent rules, evidence guide 5개입니다.

## Evidence links

- Commands: `commands.md`
- Attempts: `attempt-log.md`
- Review/QA: 현재 pre-review 단계에서는 생성되지 않았으며 PR 본문 preflight의 입력이 아닙니다.
