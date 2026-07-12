# Issue Metrics

Issue: #71
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/71
Branch: codex/issue-71-workflow-rollback
Measured at: 2026-07-12

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 4 | 20 | 1 | 0 | 1 | 0 | 0 | 5 |

## 측정 근거

- Agent 수 4는 고유 역할 Dev, Review, QA, Docs입니다. Main Coordinator와 CI는 제외하고 동일 역할 재시도는 중복 계산하지 않습니다.
- 작업 시간은 최초 Generate 관찰 `2026-07-12T16:55:28.2765399+09:00`부터 마지막 repository reverification이 기록된 QA 관찰 `2026-07-12T17:14:53+09:00`까지 19분 24.7234601초를 분 단위 올림한 20분입니다. 추정값을 사용하지 않았습니다.
- 재시도 1은 Review P1에 따른 원래 Dev의 code remediation Attempt 2입니다.
- Review 결함은 P1 1건, QA 결함은 0건입니다.
- 정체와 범위 밖 변경 파일은 0입니다.
- 읽은 핵심 문서는 Issue #71, project Issue Loop Skill, orchestration policy, agent rules, evidence guide 5개입니다.
