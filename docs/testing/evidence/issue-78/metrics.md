# Issue Metrics

Issue: #78
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/78
Branch: codex/issue-78-harness-lightweight
Measured at: 2026-07-13

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 4 | 1 | 1 | 0 | 1 | 0 | 0 | 5 |

## Pilot 전 비교 기준과 수집 가능 지표

- 비교 기준은 #55 14분, #12 24분, #13 37분·재시도 3회·정체 2회, #14의 Docs 후 재검토·CI 재실행 사례와 해당 PR 역할 댓글·CI 실행 횟수입니다.
- 연속된 적격 Issue 최소 2개에서 Agent dispatch 수, LLM Review 횟수, 역할 packet 파일 수·문자 수, source 본문 문자 수 0, Gradle/Testcontainers/Level 5/CI 실행 횟수, Docs-only commit과 동일 head CI 재실행 횟수를 수집합니다.
- 작업 시간, 재시도, 정체, 사람 승인 요청 수, Review P0/P1·QA 결함·CI 실패·merge 후 회귀 수를 GitHub와 attempt-log 정본에서 수집합니다.
- 로컬 토큰 집계가 실제로 가능할 때만 원문 session log를 공개하지 않고 Issue별 aggregate input/output token을 기록하며, 불가능하면 추정하지 않습니다.
- 속도·호출 수가 감소하지 않거나 품질 gate가 약해지면 #71 흐름으로 rollback합니다.

## 측정 근거

- Agent 수 4는 STRICT의 Dev, Review, QA, Docs 고유 역할이며 Main Coordinator와 CI 및 같은 역할 재시도는 제외합니다.
- 작업 시간은 Generate `2026-07-13T15:30:36.9501401+09:00`부터 repository gate와 diff 검사 종료 직후 `2026-07-13T15:31:37.1950448+09:00`까지 1분 0.2449047초를 정수 형식에 맞춰 1분으로 반올림한 실측값입니다.
- P1 Review finding 1건으로 허용된 remediation 1회를 수행했습니다. QA 결함과 범위 밖 변경 파일은 0건입니다.
