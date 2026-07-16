# Issue Metrics

Issue: #119
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/119
Branch: codex/issue-119-ranking-ledger
Execution head: 45b3a3f8686e2e469e029d6bb0846c8910bcfc28
Measured at: 2026-07-16

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 3 | 66 | 0 | 0 | 0 | 0 | 0 | 8 |

## 측정 근거

- Generate 시작부터 최종 ASCII clean reverification까지 66분입니다.
- STRICT 역할은 Dev, 독립 Review, 독립 QA 3개이며 Dev 단계의 Review·QA 결함 수는 아직 0입니다.
- 한글 cwd의 ClassNotFound는 같은 Attempt 안의 환경 경로 재실행으로 판별했으며 production 재시도나 declared stall은 아닙니다.
- 범위 밖 production 변경은 0개이고 핵심 문서는 AGENTS, Issue Loop, orchestration policy, agent rules, test strategy, evidence guide, ADR-008, Issue #119입니다.

## Evidence links

- Commands: `commands.md`
- Attempts: `attempt-log.md`
- Runtime and cleanup: `manual-qa.md`
- Verification: `verification.md`
