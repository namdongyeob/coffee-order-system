# Issue Metrics

Issue: #120
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/120
Branch: codex/issue-120-hang-diagnosis
Execution head: 37d410f71bcdd86ef1af02e7c807b4401bfcb927
Measured at: 2026-07-17

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 1 | 8 | 0 | 0 | 0 | 0 | 0 | 8 |

## 측정 근거

- 작업 시간은 pre-fix Generate 시작 `2026-07-16T23:53:01+09:00`부터 current cleanup Reverification 종료 `2026-07-17T00:01:11+09:00`까지의 분 단위 경과 시간입니다.
- 현재 evidence의 고유 역할은 Dev 1명입니다. STRICT 독립 Review·QA는 PR에서 Main Coordinator가 후속 호출합니다.
- pre-fix/current 두 실행은 같은 Attempt의 A/B 원인 검증이며 실패 수정 재시도가 아닙니다.
- declared stall과 `BLOCKED`는 없었고 production·test·config 범위 밖 변경도 0개입니다.
- 읽은 핵심 자료는 `AGENTS.md`, Issue Loop Skill, orchestration policy, agent rules, test strategy, evidence guide, Issue #120, #113 evidence·PR #121 diff입니다.

## Evidence links

- Commands: `commands.md`
- Attempts: `attempt-log.md`
- Runtime and cleanup: `manual-qa.md`
- Verification: `verification.md`
- Review/QA: PR 역할 보고와 GitHub checks를 정본으로 확인
