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
| Level 0 | `python scripts/harness_gate.py --issue 114 --pr-body-file $env:TEMP\coffee-order-issue-114-pr-body.md` | FAIL, BLOCKED는 PASS 행을 금지하지만 Level 5/6 YES는 PASS 행을 강제하는 하네스 모순 |

## 실제 HTTP 명령

메뉴와 인기 메뉴 조회는 아래 host를 사용했습니다.

```powershell
curl.exe -sS --max-time 10 -w "`nHTTP_STATUS=%{http_code}`n" http://localhost:8080/api/menus
curl.exe -sS --max-time 10 -w "`nHTTP_STATUS=%{http_code}`n" http://localhost:8080/api/menus/popular
```

Windows `curl.exe` native argument quoting으로 손상된 첫 POST 묶음은 최종 근거에서 제외했습니다. 최종 POST 응답은 아래 .NET `HttpClient` 명령과 JSON body로 관찰했습니다.

```powershell
Add-Type -AssemblyName System.Net.Http
$client=[System.Net.Http.HttpClient]::new()
$base='http://localhost:8080'
function Send-JsonPost([string]$label,[string]$path,[string]$json){
  Write-Output ('<<<' + $label + '>>>')
  $content=[System.Net.Http.StringContent]::new($json,[System.Text.Encoding]::UTF8,'application/json')
  $response=$client.PostAsync($base+$path,$content).GetAwaiter().GetResult()
  $body=$response.Content.ReadAsStringAsync().GetAwaiter().GetResult()
  Write-Output $body
  Write-Output ('HTTP_STATUS=' + [int]$response.StatusCode)
}
Send-JsonPost 'INVALID CHARGE 0' '/api/points/charge' '{"userId":114011,"amount":0}'
Send-JsonPost 'PREPARE LOW BALANCE' '/api/points/charge' '{"userId":114012,"amount":1}'
Send-JsonPost 'INSUFFICIENT ORDER' '/api/orders' '{"userId":114012,"menuId":1}'
Send-JsonPost 'CHARGE SUCCESS' '/api/points/charge' '{"userId":114011,"amount":10000}'
Send-JsonPost 'ORDER SUCCESS' '/api/orders' '{"userId":114011,"menuId":1}'
$client.Dispose()
```

## 실제 DB 명령

manual 주문·포인트·Outbox·processed event는 아래 MySQL 8.4 compose service와 SQL로 조회했습니다. 마지막 ledger 정렬은 실제 스키마에 없는 `created_at` 때문에 실패했고 바로 다음 교정 명령으로만 ledger를 판정했습니다.

```powershell
docker compose -f docker/compose.yaml exec -T mysql mysql -ucoffee -pcoffee coffee_order -e "SELECT id,user_id,menu_id,paid_amount,status,ordered_at FROM orders WHERE user_id IN (114011,114012) ORDER BY id; SELECT id,user_id,balance FROM user_point WHERE user_id IN (114011,114012) ORDER BY user_id; SELECT id,event_id,event_type,created_at,published_at,payload FROM outbox_event ORDER BY id; DESCRIBE processed_event; SELECT * FROM processed_event ORDER BY processed_at; DESCRIBE ranking_event_ledger; SELECT * FROM ranking_event_ledger ORDER BY created_at;"
docker compose -f docker/compose.yaml exec -T mysql mysql -ucoffee -pcoffee coffee_order -e "SELECT event_id,event_type,state,source,reserved_at,redis_applied_at,committed_at FROM ranking_event_ledger ORDER BY reserved_at;"
```

최종 drain count는 아래 SQL로 관찰했습니다.

```powershell
docker compose -f docker/compose.yaml exec -T mysql mysql -ucoffee -pcoffee coffee_order -e "SELECT COUNT(*) AS orders_count FROM orders; SELECT COUNT(*) AS outbox_count, SUM(published_at IS NULL) AS unpublished_count FROM outbox_event; SELECT COUNT(*) AS processed_count FROM processed_event; SELECT state,COUNT(*) AS ledger_count FROM ranking_event_ledger GROUP BY state;"
```

## 실제 Kafka·Redis 명령

topic `order.completed`, consumer group `ranking-consumer-group`과 Redis key `popular:menus:2026-07-18`을 아래 명령으로 확인했습니다.

```powershell
docker compose -f docker/compose.yaml exec -T kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
docker compose -f docker/compose.yaml exec -T kafka /opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group ranking-consumer-group --describe
docker compose -f docker/compose.yaml exec -T kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic order.completed --from-beginning --max-messages 1 --timeout-ms 5000
docker compose -f docker/compose.yaml exec -T redis redis-cli --scan --pattern 'popular:menus:*'
docker compose -f docker/compose.yaml exec -T redis redis-cli ZRANGE 'popular:menus:2026-07-18' 0 -1 WITHSCORES
```

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
- Attempt 2에서 새 HTTP/DB/Kafka/Redis/runtime/k6 명령은 실행하지 않았습니다. 위 상세 명령은 Attempt 1 tool transcript에서 실제 사용 문자열만 옮겼습니다.
- 이전 컨테이너 `exit 255` 당시의 container ID, inspect/log/resource 명령 출력은 남아 있지 않아 원인 재현 명령으로 기록할 수 없습니다.
- PR body는 저장소 밖 UTF-8 no-BOM 파일로 정합화했지만 preflight FAIL 때문에 `gh pr edit`을 실행하지 않았습니다.
