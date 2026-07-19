# Issue Metrics

Issue: #137
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/137
Branch: codex/issue-137-harness-lightweight
Measured at: 2026-07-19

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 3 | 미측정 | 3 | 0 | 8 | 0 | 0 | 5 |

## 측정 근거

- 최초 Generate 시각을 실시간 기록하지 않아 작업 시간은 추정하지 않고 `미측정`으로 남깁니다.
- Dev·Review·QA 세 역할을 기존 #137 bootstrap 계약대로 셉니다. Review·QA는 draft PR 뒤 pending입니다.
- Review가 반환한 P1 3건을 Attempt 2에서, 명시적으로 재승인된 P1 4건을 Attempt 3에서, 마지막 script 분류 P1 1건을 Attempt 4에서 수정했습니다. 재시도 3, Review 결함 8, 정체·범위 밖 파일은 0입니다.
- 직접 정본은 Issue loop Skill, orchestration policy, agent rules, test strategy, evidence guide 5개입니다.

## Evidence links

- Commands: `commands.md`
- Attempts: `attempt-log.md`
- Review/QA: draft PR 생성 뒤 GitHub 역할 보고
