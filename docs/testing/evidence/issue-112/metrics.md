# Issue Metrics

Issue: #112
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/112
Branch: issue-112-attempt2
Measured at: 2026-07-16

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 2 | 미측정 | 5 | 1 | 11 | 0 | 0 | 11 |

## 측정 근거

- Generate 시작 시각을 별도 기록하지 않아 작업 시간은 추정하지 않고 `미측정`으로 기록합니다. 개별 Gradle 소요 시간과 Level 5 관찰은 `commands.md`와 `manual-qa.md`에 남겼습니다.
- 현재 고유 역할은 Dev와 독립 Review 2명입니다. Attempt 6 독립 re-review와 독립 QA는 아직 pending이며, Main Coordinator와 CI는 Agent 수에서 제외합니다.
- Attempt 2는 conflict prevalidation과 pending recovery short-circuit, Attempt 3은 DB 시간 정밀도 false conflict, Attempt 4는 독립 Review crash/offset/heartbeat 지적, Attempt 5는 retry-boundary 지적, Attempt 6은 durable artifact·recovery lock 지적 보완으로 재시도 5회입니다.
- 정체 1회는 test-only SharedTestcontainers class monitor 교착으로, thread dump 후 최소 범위 수정으로 해소했습니다.
- 독립 Review는 기존 P1 4건·P2 1건, 재검토 P1 3건·P2 1건, 이번 P1 1건·P2 1건을 보고해 Review 결함 11건입니다. 독립 QA는 아직 실행 전이라 결함 0이며 pending 상태는 GitHub PR에서 후속 확인합니다.
- 범위 밖 변경 파일은 0개입니다. production 변경은 Rebuild 전용 package와 V6 migration이며 test-only SharedTestcontainers 수정 1개가 포함됩니다.
- 핵심 문서 11개는 repository AGENTS, Issue Loop Skill, Context Router, 구현 가드레일, Evidence Guide, Orchestration Policy, Issue 개발 흐름, 테스트 전략, ADR-008과 관련 Kafka·복구 문서입니다.

## Evidence links

- Commands: `commands.md`
- Attempts: `attempt-log.md`
- Runtime and cleanup: `manual-qa.md`
- Verification: `verification.md`
- Review/QA/CI: GitHub PR comments와 checks를 정본으로 확인
