# Issue Metrics

Issue: #52
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/52
Branch: claude/issue-52-harness-slim-3
Measured at: 2026-07-13

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 3 | 미측정 | 1 | 0 | 1 | 0 | 0 | 5 |

## 측정 근거

- 시작 시각이 당시 기록되지 않아 작업 시간은 `미측정`입니다.
- Dev, Review, QA의 고유 역할 세 개를 기록했습니다. metadata 불일치가 없어 Docs Agent는 호출하지 않았습니다. Main Coordinator와 CI는 제외합니다.
- 재시도 1은 fresh Review의 `REVISE` 뒤 Attempt 2(head `30ecf80`)의 P1·P2 정정 1회입니다.
- Review 결함 1은 fresh Review가 반환한 P1(flaky 절차 참조 오지정)입니다. 함께 반환된 P2 2건은 같은 정정으로 해소했습니다. 최종 Review는 `APPROVED`입니다.
- QA 결함 0입니다. 독립 QA는 `PASS`이며, 지적한 evidence 최신성 갭(head/크기 수치)은 이 evidence 갱신으로 반영해 판정 변경 없이 해소했습니다.
- 읽은 핵심 문서는 `AGENTS.md`, Issue loop Skill, orchestration policy, agent-rules, test strategy입니다.
- CI는 push 뒤 GitHub 새 head에서 확인하며 이전 head 결과를 현재 head 결과로 복제하지 않습니다.
