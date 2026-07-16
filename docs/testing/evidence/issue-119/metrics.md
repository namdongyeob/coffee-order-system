# Issue Metrics

Issue: #119
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/119
Branch: codex/issue-119-ranking-ledger
Execution head: 8506132df37034e31ee2e8037eb6a37dead2050f
Measured at: 2026-07-16

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 3 | 86 | 1 | 0 | 2 | 0 | 0 | 8 |

## 측정 근거

- Generate 시작부터 Review 수정의 최종 ASCII clean reverification까지 86분입니다.
- STRICT 역할은 Dev, 독립 Review, 독립 QA 3개이며 Review가 반환한 P1·P2 결함 2건을 Attempt 2에서 수정했습니다. QA 결함은 아직 0입니다.
- 한글 cwd의 ClassNotFound는 같은 Attempt 안의 환경 경로 재실행으로 판별했으며 production 재시도나 declared stall은 아닙니다.
- 범위 밖 production 변경은 0개이고 핵심 문서는 AGENTS, Issue Loop, orchestration policy, agent rules, test strategy, evidence guide, ADR-008, Issue #119입니다.

## Evidence links

- Commands: `commands.md`
- Attempts: `attempt-log.md`
- Runtime and cleanup: `manual-qa.md`
- Verification: `verification.md`
