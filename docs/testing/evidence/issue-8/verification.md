# 검증 로그

| 날짜 | Issue | Level | 결과 | 검증 범위 | 명령/Evidence | 비고 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-11 | Issue #8 Kafka order completed event | Level 4 | PASS | Kafka producer·이벤트 계약 인프라 통합 | QA actual Kafka suite; `docs/testing/evidence/issue-8/test-output.txt` | Attempt 3 뒤 독립 QA가 8 tests, failures 0과 `BUILD SUCCESSFUL in 1m 7s`를 확인했습니다. Publisher+Service focused 6 tests도 23초에 PASS했고 sync·async marker를 각각 1회 확인했습니다. 최종 Review는 APPROVED, findings 없음입니다. |
| 2026-07-11 | Issue #8 Kafka order completed event | Level 1 | PASS | 빌드·Unit·전체 회귀 smoke | `./gradlew.bat test --no-daemon`; `docs/testing/evidence/issue-8/test-output.txt` | Attempt 3 뒤 독립 QA가 전체 30 tests, failures 0과 `BUILD SUCCESSFUL in 1m 13s`를 확인했습니다. CI quality-gates도 1m 29s에 PASS했습니다. |
| 2026-07-11 | Issue #8 Kafka order completed event | Level 5 | PASS | 로컬 앱·인프라 기동 | `docs/testing/evidence/issue-8/manual-qa.md` | 독립 QA가 앱과 MySQL/Kafka/Redis 기동, `Started CoffeeOrderSystemApplication in 43.23 seconds`, health HTTP 200 `UP`을 확인하고 리소스를 정리했습니다. |
| 2026-07-11 | Issue #8 Kafka order completed event | Level 6 | PASS | 실제 HTTP·Kafka 이벤트 관찰 | `http/issue-8-order-completed-event.http`; `docs/testing/evidence/issue-8/manual-qa.md` | userId 808 충전 200, 주문 201과 Kafka key `808`, eventId `13247f60-c5a7-4a7c-a771-39b225d191a4`의 JSON value 원문을 독립 QA가 확인했습니다. |
| 2026-07-11 | Issue #8 Kafka broker-down bounded observation | Level 6 | PARTIAL | 실제 broker 중단 중 주문 HTTP·DB 관찰 | `docs/testing/evidence/issue-8/manual-qa.md`; `docs/testing/evidence/issue-8/test-output.txt` | health 200, 충전 200 뒤 주문은 15,048ms에 client timeout되어 최종 HTTP status와 sync throw/log를 관찰하지 못했습니다. DB에는 order id 1, userId 909, `PAID`, 잔액 5500이 남았습니다. 기본 `max.block.ms=60000` outage latency 위험은 후속 후보이며 QA 리소스는 정리했습니다. |
