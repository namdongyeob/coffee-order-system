# Issue Metrics

Issue: #12
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/12
Branch: codex/issue-12-http-artifacts
Measured at: 2026-07-12

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 4 | 24 | 2 | 0 | 0 | 0 | 0 | 8 |

## 측정 근거

- 최초 Generate 시작은 `2026-07-12T20:09:16+09:00`, 마지막 Docs Reverification 종료는 `2026-07-12T20:32:59.5427882+09:00`이며 실제 경과 23분 43.543초를 분 단위 올림해 24분으로 기록했습니다.
- STRICT Agent 수는 Dev, Review, QA, Docs 역할 4명이며 Main Coordinator와 CI는 제외합니다.
- 재시도 2회는 `attempt-log.md`의 최초 Attempt 뒤 실행한 Attempt 2와 Attempt 3입니다. Dev의 PowerShell inline JSON quoting 실패와 QA의 capture-only 실패는 환경 실패이며, 각각 clean canonical 실행과 허용된 clean QA 재실행으로 회복했습니다.
- fresh Review 결함과 independent QA 제품 결함은 각 0건입니다. 정체와 범위 밖 변경 파일도 0건입니다.
- 읽은 핵심 문서는 Context Router, Postman 검증 가이드, API 명세, 요구사항, 주문 정책, 포인트 정책, 테스트 전략, evidence 안내 8개입니다.

## Evidence links

- Commands: `commands.md`
- Attempts: `attempt-log.md`
- Review: https://github.com/namdongyeob/coffee-order-system/pull/74#issuecomment-4950991656
- QA: https://github.com/namdongyeob/coffee-order-system/pull/74#issuecomment-4951011488
