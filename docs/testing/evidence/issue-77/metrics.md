# Issue Metrics

Issue: #77
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/77
Branch: codex/issue-77-dlt-flaky
Measured at: 2026-07-13

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 4 | 17 | 0 | 0 | 0 | 0 | 0 | 13 |

## 측정 근거

- 작업 시간은 최초 Generate `2026-07-13T13:13:39.3244606+09:00`부터 repository와 PR body Reverification 종료 `2026-07-13T13:30:28.3746642+09:00`까지 실제 관찰한 16분 49.0502036초를 저장소 정수 형식에 맞춰 17분으로 반올림했습니다. 추정값을 사용하지 않았습니다.
- Agent 수 4는 STRICT의 고유 역할 Dev, Review, QA, Docs입니다. Main Coordinator와 CI를 제외합니다.
- 기존 RED는 live Issue에 기록된 baseline이며 같은 Attempt의 원인 분석 입력입니다. 수정 Attempt는 1회라 재시도 수는 0입니다.
- 정체, 범위 밖 변경 파일은 0개입니다. fresh Review와 independent QA가 아직 실행되지 않아 결함 수는 각각 0이며 pending 상태입니다.
- 읽은 핵심 문서 13개는 live Issue #77, `AGENTS.md`, Issue Loop Skill, Context Router, Kafka·복구 정본 4개, Issue 실행 흐름, 오케스트레이션 정책, 테스트 전략, evidence 안내와 완료 전 체크리스트입니다.

## Evidence links

- Commands: `commands.md`
- Attempts: `attempt-log.md`
- Runtime and cleanup: `manual-qa.md`
- Review/QA: draft PR 뒤 별도 역할 보고 예정
