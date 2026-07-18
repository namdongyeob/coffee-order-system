# Issue #114 Commands

Issue: #114
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/114
Execution head: e9412ab3cc4ceb56de5b4ae9659a0e9e3a5d59ec

| Level | 명령 또는 확인 | 결과 |
| --- | --- | --- |
| Level 0 | `git status --porcelain=v1 -uall`; `git rev-parse HEAD` | PASS, 변경·untracked 0건, exact head `e9412ab3...` |
| Level 5 | `docker compose -f docker/compose.yaml down -v --remove-orphans`; `docker compose -f docker/compose.yaml up -d mysql redis kafka`; `docker compose -f docker/compose.yaml ps` | PASS, MySQL·Redis·Kafka 모두 `running/healthy` |
| Level 5 | `$env:SPRING_PROFILES_ACTIVE='local'; .\gradlew.bat bootRun --no-daemon`; `curl.exe ... /actuator/health` | PASS, Flyway 7 migrations, startup 12.262s, HTTP 200 `UP` |
| Level 6 | `GET /api/menus` | PASS, HTTP 200, seed 메뉴 4개 |
| Level 6 | `POST /api/points/charge` user 114011 amount 10000 | PASS, HTTP 200, balance 10000 |
| Level 6 | `POST /api/orders` user 114011 menu 1 | PASS, HTTP 201, order 1, PAID 4500 |
| Level 6 | `GET /api/menus/popular` | PASS, HTTP 200, rank 1 menu 1 orderCount 1 |
| Level 6 | `POST /api/points/charge` amount 0 | PASS, HTTP 400 `INVALID_CHARGE_AMOUNT` |
| Level 6 | 1P 충전 뒤 `POST /api/orders` user 114012 menu 1 | PASS, HTTP 409 `INSUFFICIENT_POINT`, balance 1 유지 |
| Level 3 | MySQL `orders`, `user_point`, `outbox_event`, `processed_event`, `ranking_event_ledger` 조회 | PASS, manual order PAID, balance 5500, Outbox published, processed, ledger `COMMITTED` |
| Level 4 | Kafka topic sample과 `kafka-consumer-groups.sh --describe` | PASS, 같은 event ID·order ID, 최종 offset 517/517, lag 0 |
| Level 4 | Redis `SCAN popular:menus:*`, `ZRANGE ... WITHSCORES` | PASS, manual score `1`, 최종 score `517` |
| Level 7 | `k6 inspect k6/order-load.js`; `k6 run --summary-export %TEMP%\coffee-order-issue-114\load-summary.json k6/order-load.js` | PASS, safe 2 VU, p95 60.45ms < 1000ms, HTTP/order error 0%, checks 158/158 |
| Level 7 | `k6 inspect k6/order-stress.js`; `k6 run --summary-export %TEMP%\coffee-order-issue-114\stress-summary.json k6/order-stress.js` | PASS, safe 6 VU, p95 59.74ms < 1500ms, HTTP/order error 0%, checks 591/591 |
| Level 7 | `k6 inspect k6/order-spike.js`; `k6 run --summary-export %TEMP%\coffee-order-issue-114\spike-summary.json k6/order-spike.js` | PASS, safe 8 VU, p95 72.04ms < 2000ms, HTTP/order error 0%, checks 815/815 |
| Level 0 | 앱 PID 종료 뒤 `docker compose -f docker/compose.yaml down -v --remove-orphans`와 container·network·volume·port·process 조회 | PASS, orphan 0건 |

## k6 공통 threshold와 실측

| 시나리오 | checks `>0.99` | HTTP failed `<0.01` | p95 | 주문 성공 `>0.99` | 주문 오류 `<0.01` | exit |
| --- | --- | --- | --- | --- | --- | --- |
| Load | 100%, 158/158 | 0% | 60.45ms `<1000ms` | 100%, 52/52 | 0% | 0 |
| Stress | 100%, 591/591 | 0% | 59.74ms `<1500ms` | 100%, 195/195 | 0% | 0 |
| Spike | 100%, 815/815 | 0% | 72.04ms `<2000ms` | 100%, 269/269 | 0% | 0 |

## 환경 메모

- Docker Server 29.4.2, 16 CPU, 약 7.7GiB memory 환경에서 실행했습니다.
- peak 전 관찰 memory는 Kafka 466.1MiB, MySQL 505.9MiB, Redis 7.234MiB였고 restart 0, exit 0, OOM false였습니다.
- k6는 `v2.0.0`, safe profile을 사용했고 각 시나리오를 한 번에 하나씩 순차 실행했습니다.
- 시작 직후 Kafka consumer가 단일 broker coordinator 선출 중 INFO `NOT_COORDINATOR` 재가입을 기록했지만 15:56:55 partition을 할당받았습니다. 이후 같은 로그 0건, 최종 lag 0, actual ERROR level 0건입니다.
- 전체 Gradle 회귀는 실행하지 않았습니다. 최신 PR-head GitHub Actions `quality-gates`가 최종 Level 1을 소유합니다.
