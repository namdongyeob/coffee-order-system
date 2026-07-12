# Issue Metrics

Issue: #66
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/66
Branch: codex/issue-66-metadata-recovery
Measured at: 2026-07-12

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 4 | 19 | 2 | 0 | 1 | 1 | 0 | 6 |

## 측정 근거

- Agent 수 4는 Dev, Review, QA, Docs 역할입니다. Main Coordinator와 CI는 제외하며 동일 역할의 재시도는 중복 계산하지 않습니다. remediation 이전 Review·QA 역할 보고는 아래 실제 PR 댓글로 연결하며, metadata recovery 새 HEAD의 official fresh Review·QA는 pending입니다.
- 작업 시간은 Attempt 1 Generate 시작 `2026-07-12T14:38:02.7076476+09:00`부터 Attempt 4 Reverification 종료 `2026-07-12T14:56:35.2839046+09:00`까지 실제 18분 32.576초이며, 분 단위 표에는 올림해 19분으로 기록했습니다. Attempt 3의 별도 시작 시각은 명령 전에 기록하지 않아 추정하지 않았습니다.
- 재시도 1은 Review P1과 QA FAIL이 함께 지적한 EOF blank-line 2건을 원래 Dev에게 한 번 반환한 Attempt 2입니다. TDD RED는 최초 Attempt의 계약 부재 확인이므로 재시도가 아닙니다.
- 재시도 2에는 사용자 승인 scope addition인 Attempt 3을 포함합니다. 이는 metadata 자동 복구나 official Review의 두 번째 수정 반환이 아닙니다.
- 별도 metadata-only recovery budget은 Attempt 4에서 1/2회를 사용했습니다. 이 횟수는 위 코드·정책 재시도 수와 분리합니다.
- Review 결함 1과 QA 결함 1은 같은 base diff 오류를 각 역할이 독립적으로 발견한 결과입니다. remediation head의 fresh 판정은 pending입니다.
- 범위 밖 변경 파일은 0입니다.
- 읽은 핵심 문서 6개는 AGENTS, coffee-order Issue loop Skill, Context Router, orchestration policy, agent rules, evidence guide입니다.

## Evidence links

- Commands: `commands.md`
- Attempts: `attempt-log.md`
- Historical Review role report: https://github.com/namdongyeob/coffee-order-system/pull/67#issuecomment-4950147743
- Historical QA role report: https://github.com/namdongyeob/coffee-order-system/pull/67#issuecomment-4950147800
