# Issue Metrics

Issue: #40
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/40
Branch: codex/issue-40-kafka-consumer-idempotency
Measured at: 2026-07-11

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 4 | 27 | 2 | 0 | 5 | 0 | 0 | 11 |

## 측정 근거

- Attempt 1의 `882.357s`와 Attempt 2의 `377.941s`를 합산한 active duration은 `1260.298s`, 즉 `21.0049667분`이며 정수 분 값은 `21`입니다. Attempt 사이 공백은 포함하지 않았습니다.
- Attempt 3은 `2026-07-11T15:50:41.290+09:00`부터 `2026-07-11T15:56:31.725+09:00`까지 정확히 `350.435s`입니다.
- 세 Attempt active duration 합은 `1610.733s`, 즉 `26.84555분`이며 정수 분 값은 `27`입니다. Attempt 사이 공백은 포함하지 않았습니다.
- Agent 수 4는 STRICT의 Dev, Review, QA, Docs 역할입니다. Main Coordinator는 역할 구성 수치에서 제외했습니다.
- 정체와 범위 밖 변경 파일은 각각 0건입니다.
- 초기 Review 수정 필요 항목은 P1 결정적 duplicate evidence와 P2 direct concurrent call 보장 경계의 2건입니다. sentinel 보강 뒤 최종 내부 Review는 PASS했고 추가 finding은 없습니다.
- Claude 승인 리뷰의 비차단 MINOR 3건은 listener assignment 대기, topic 상수 공유, 기본 error handler 장기 장애 위험 문서화이며 Attempt 3에서 반영했습니다.
- 재시도 수 2는 Review FAIL 보완 Attempt 2와 Claude 승인 MINOR 후속 Attempt 3입니다.
- 독립 QA 수정 필요 항목은 0건이며 focused unit, Level 3, Level 4, fresh Level 1, Level 5가 PASS했습니다.
- 읽은 핵심 문서 11개는 AGENTS/Issue loop, Kafka hot path 5개, ERD, orchestration, agent rules, test strategy, evidence guide입니다.
- GitHub Actions CI와 사람의 최종 승인은 이 metrics에서 완료로 집계하지 않았습니다.

## Evidence links

- Commands: `commands.md`.
- Attempts: `attempt-log.md`.
- Review/QA and residual risk: `manual-qa.md`.
- Test output summary: `test-output.txt`.
