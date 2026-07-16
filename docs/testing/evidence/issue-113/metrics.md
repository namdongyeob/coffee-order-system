# Issue Metrics

Issue: #113
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/113
Branch: issue-113
Measured at: 2026-07-16

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 1 | 미측정 | 2 | 0 | 0 | 0 | 0 | 8 |

## 측정 근거

- 작업 시간은 Generate 시작 시각을 별도로 기록하지 않아 추정하지 않고 `미측정`으로 기록합니다. Gradle이 보고한 Attempt 3 clean 묶음 3분 8초와 이전 focused 결과는 commands evidence에 남겼습니다.
- 현재 문서 작성 시점의 고유 역할은 Dev 1명입니다. Review·QA·Docs는 아직 실행 전이며 Main Coordinator와 CI는 Agent 수에서 제외합니다.
- Attempt 2는 Attempt 1에서 관찰된 stale Kafka record 결함을 purge로 보완한 1회 재시도이고, Attempt 3은 non-listener context listener 비활성화와 listener context 종료 경계를 보완한 2회째 재시도입니다. 정체는 없었고 범위 밖 변경 파일도 0개입니다.
- 읽은 핵심 문서 8개는 `AGENTS.md`, Issue Loop Skill, Context Router, 구현 가드레일, Evidence Guide, Orchestration Policy, Issue 개발 흐름, 테스트 전략입니다.

## Evidence links

- Commands: `commands.md`
- Attempts: `attempt-log.md`
- Runtime and cleanup: `manual-qa.md`
- Review/QA/CI: GitHub PR comments와 checks를 정본으로 확인
