# Issue Metrics

Issue: #11
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/11
Branch: codex/issue-11-kafka-retry-dlt-v2
Measured at: 2026-07-12

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 4 | 140 | 1 | 1 | 0 | 0 | 0 | 5 |

## 측정 근거

- 작업 시간은 최초 Generate `2026-07-12T15:26:30.0273613+09:00`부터 마지막 Reverification `2026-07-12T17:46:01.5355625+09:00`까지 실제 139분 31.508초이며, 분 단위 표에는 올림해 140분으로 기록했습니다.
- STRICT Agent 수는 Dev, Review, QA, Docs 역할 수 4이며 Main Coordinator와 CI는 제외합니다. Review, QA, Docs는 pending입니다.
- 재시도 1은 안전 정지 뒤 사람이 승인한 최신 main merge-conflict resolution과 재검증 Attempt 2입니다. 정체 1은 최초 일반 merge의 단일 conflict로 선언한 안전 정지입니다.
- Review·QA 결함 수는 해당 역할이 아직 실행되지 않아 0이며 pending입니다.
- 범위 밖 변경 파일은 현재 0입니다.
- 읽은 핵심 문서 5개는 Kafka 이벤트 흐름, Kafka 선택 ADR, replay 복구 ADR, 복구 전략, Kafka Redis runbook입니다.

## Evidence links

- Commands: `commands.md`
- Attempts: `attempt-log.md`
- Review/QA: pending
