# Issue Metrics

Issue: #35
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/35
Branch: codex/issue-35-verifier-routing
Measured at: 2026-07-11

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 4 | 미측정 | 0 | 0 | 0 | 0 | 0 | 4 |

## 측정 근거

- Dev, Review, QA, Docs Agent 각 한 명, 총 4명이 역할을 수행했습니다. Main Coordinator는 저장소 작업 Agent 수에 포함하지 않았습니다.
- 최초 Attempt 뒤 구현 수정 Attempt는 없어 재시도 수는 0입니다. QA가 존재하지 않는 클래스명을 지정해 발생한 loader error는 명령 교정이며 저장소 결함이나 구현 재시도 Attempt로 세지 않았습니다.
- 독립 Review와 QA는 모두 PASS했고 수정 필요 항목을 반환하지 않아 Review 결함 수와 QA 결함 수는 각각 0입니다.
- PR #32와 #33의 실제 본문은 PR 생성 시점에 Combined Verifier와 CI를 pending으로 기록했습니다. 이 반복 관찰과 Issue #35의 정책 결정은 `attempt-log.md`에 연결했습니다.
- 읽은 핵심 문서는 Issue가 지정한 `orchestration-policy.md`, `context-router.md`, `agent-rules.md`, `evidence-guide.md` 네 파일입니다.

## Evidence links

- Commands: `commands.md`
- Attempts: `attempt-log.md`
- Review/QA: `commands.md`, `manual-qa.md`
