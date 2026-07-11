# Issue Metrics

Issue: #8
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/8
Branch: codex/issue-8-kafka-order-event
Measured at: 2026-07-11

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 4 | 미측정 | 2 | 0 | 4 | 0 | 0 | 9 |

## 측정 근거

- 최초 Generate artifact인 `acceptance-criteria.md`의 filesystem `CreationTime`은 명령으로 독립 확인한 2026-07-11 08:06:56입니다. 마지막 Reverification 종료 시각은 독립적으로 재구성할 수 없어 작업 시간은 추정하지 않고 `미측정`으로 기록했습니다.
- `git show -s --format=%cI 8576010`으로 2026-07-11T08:33:24+09:00 commit 시각을 재현할 수 있지만, 이 시각을 마지막 Reverification 종료 시각으로 사용하지 않았습니다. timestamp 확인 명령과 결과는 `commands.md`에 기록했습니다.
- 재시도는 Review FAIL 뒤 같은 Dev에게 반환된 Attempt 2 한 건입니다.
- Agent 수는 Dev, 독립 Review, 독립 QA, Docs 역할 4명입니다. Review가 반환한 P1 1건과 P2 2건을 Review 결함 3건으로 기록했고 최종 Review는 추가 결함 없이 PASS했습니다. QA 결함은 0건입니다.
- 범위 밖 파일은 수정하지 않았습니다.
- 읽은 핵심 문서는 Kafka/복구/주문 정책 5개와 orchestration/agent/test/evidence 정본 4개입니다.
- Attempt 3 실측 시작 시각은 Main Coordinator가 현재 턴 시작에 전달한 `2026-07-11T10:30:07.190+09:00`입니다. fresh 전체 회귀 직후 `Get-Date -Format o`로 캡처한 종료 시각은 `2026-07-11T10:35:48.7319732+09:00`이며 Attempt 3 duration은 `00:05:41.5419732`입니다. 과거 전체 작업 시간 `미측정`은 소급 변경하지 않았습니다.
- 재시도 2건은 기존 Review FAIL Attempt 2와 사용자가 승인한 Claude MAJOR 수정 Attempt 3입니다. Review 결함은 기존 3건에 동기 send 예외 전파 MAJOR 1건을 더한 4건입니다.

## Evidence links

- Commands: `commands.md`
- Attempts: `attempt-log.md`
- Review/QA: draft PR의 후속 역할 보고에 연결합니다.
