# Issue Metrics

Issue: #8
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/8
Branch: codex/issue-8-kafka-order-event
Measured at: 2026-07-11

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 2 | 26 | 1 | 0 | 3 | 0 | 0 | 9 |

## 측정 근거

- 작업 시간은 최초 Generate artifact인 `acceptance-criteria.md`의 filesystem `CreationTime` 2026-07-11 08:06:56부터 Review FAIL 수정 뒤 마지막 전체 회귀 종료 로그 2026-07-11 08:32:27까지 25분 31초이며, 분 단위 경과 시간을 올림해 26분으로 기록했습니다.
- 재시도는 Review FAIL 뒤 같은 Dev에게 반환된 Attempt 2 한 건입니다.
- Agent 수는 Dev와 독립 Review 역할 2명입니다. Review가 반환한 P1 1건과 P2 2건을 Review 결함 3건으로 기록했습니다. QA는 수정 SHA 대기 중이므로 0입니다.
- 범위 밖 파일은 수정하지 않았습니다.
- 읽은 핵심 문서는 Kafka/복구/주문 정책 5개와 orchestration/agent/test/evidence 정본 4개입니다.

## Evidence links

- Commands: `commands.md`
- Attempts: `attempt-log.md`
- Review/QA: draft PR의 후속 역할 보고에 연결합니다.
