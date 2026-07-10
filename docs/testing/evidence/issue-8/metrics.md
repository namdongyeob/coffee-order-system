# Issue Metrics

Issue: #8
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/8
Branch: codex/issue-8-kafka-order-event
Measured at: 2026-07-11

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 1 | 17 | 0 | 0 | 0 | 0 | 0 | 9 |

## 측정 근거

- 작업 시간은 최초 Generate artifact인 `acceptance-criteria.md`의 filesystem `CreationTime` 2026-07-11 08:06:56부터 self-review 뒤 마지막 전체 회귀 종료 로그 2026-07-11 08:23:25까지 16분 29초이며, 분 단위 경과 시간을 올림해 17분으로 기록했습니다.
- 재시도는 별도 Attempt를 다시 배정하지 않았으므로 0입니다. 같은 Attempt 안의 RED/GREEN 반복과 테스트 harness 수정은 재배정 Attempt로 세지 않았습니다.
- Review와 QA는 아직 pending이므로 결함 수를 0으로 기록했습니다.
- 범위 밖 파일은 수정하지 않았습니다.
- 읽은 핵심 문서는 Kafka/복구/주문 정책 5개와 orchestration/agent/test/evidence 정본 4개입니다.

## Evidence links

- Commands: `commands.md`
- Attempts: `attempt-log.md`
- Review/QA: draft PR의 후속 역할 보고에 연결합니다.
