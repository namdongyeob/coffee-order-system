# Issue Metrics

Issue: #92
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/92
Branch: claude/issue-92-merge-governance-baseline
Measured at: 2026-07-14

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 3 | 미측정 | 1 | 0 | 1 | 0 | 0 | 6 |

## 측정 근거

- 시작 시각이 기록되지 않아 작업 시간은 `미측정`입니다.
- STRICT 기본 구성은 Dev, Review, QA의 고유 역할 수 3입니다. Main Coordinator는 제외합니다. Docs Agent는 dispatch하지 않았으므로 4로 올리지 않습니다.
- 재시도 1은 Current Attempt(2) - 1입니다. fresh Review Agent가 Attempt 1에서 evidence head/결함 수 placeholder를 이유로 CHANGES_REQUESTED를 반환해 같은 Dev(본인)에게 한 번 반환된 Bounded Retry이며, Attempt 2에서 내용 재작성 없이 evidence 필드만 최종화했습니다.
- Review 결함 수 1은 위 P1(evidence head/결함 수 placeholder, 내용 결함 아님)입니다. QA 결함 수 0은 fresh QA Agent(PASS, P0/P1/P2 없음)입니다.
- 읽은 핵심 문서는 하네스·스크립트 hot path 필수 4개(orchestration-policy.md, agent-rules.md, test-strategy.md, evidence-guide.md) + 공통 진입 2개(AGENTS.md, context-router.md) = 6개입니다. `harness-metrics-and-transfer.md`, Issue #56/#93 본문은 근거 확인용으로 참조했지만 hot path 카운트에는 포함하지 않았습니다.
