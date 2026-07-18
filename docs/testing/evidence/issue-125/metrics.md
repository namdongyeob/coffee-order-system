# Issue Metrics

Issue: #125
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/125
Branch: codex/issue-125-ranking-ledger-retention
Measured at: 2026-07-18

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 3 | 117 | 3 | 0 | 5 | 0 | 0 | 12 |

## 측정 근거

- 작업 시간은 최초 Generate `2026-07-18T13:35:26+09:00`부터 Attempt 4 targeted Reverification `2026-07-18T15:32:56+09:00`까지 분 단위 경과 시간입니다.
- 재시도 3은 Review 반환 Attempt 2와 사용자가 명시 승인한 추가 Attempt 3·4입니다. declared stall이나 BLOCKED는 없었습니다.
- STRICT 역할 수는 Dev, Review, QA의 고유 역할 3입니다. Review 반환 결함은 Attempt 2의 2건, Attempt 3의 2건, Attempt 4의 1건으로 총 5건이며 QA finding은 아직 0입니다.
- 범위 밖 변경 파일은 0개입니다. 핵심 문서 수는 프로젝트/검증 정본과 Redis ranking/ADR 직접 문서 12개입니다.

## Evidence links

- Commands: `commands.md`
- Attempts: `attempt-log.md`
- Runtime and cleanup: `manual-qa.md`
- Verification: `verification.md`
- Review/QA: draft PR의 후속 역할 보고
