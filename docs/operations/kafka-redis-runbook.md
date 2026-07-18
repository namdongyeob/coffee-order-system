# Kafka Redis Runbook

## Ranking event ledger retention

`ranking_event_ledger` cleanup은 한 scheduler tick에서 한 번의 bounded batch만 실행합니다. 기본 설정은 다음과 같습니다.

| 설정 | 기본값 | 의미 |
| --- | --- | --- |
| `ranking.ledger.cleanup.enabled` | `true` | scheduler 활성화 여부 |
| `ranking.ledger.cleanup.ledger-retention` | `30d` | `COMMITTED` ledger 최소 보존 기간 |
| `ranking.ledger.cleanup.redis-marker-ttl` | `30d` | `ranking:applied-event:{eventId}` marker TTL |
| `ranking.ledger.cleanup.kafka-retention` | `30d` | 운영자가 확인한 `order.completed` effective retention |
| `ranking.ledger.cleanup.dlt-retention` | `30d` | 운영자가 확인한 `order.completed.DLT` effective retention |
| `ranking.ledger.cleanup.maximum-rebuild-recovery-window` | `30d` | rebuild를 재개할 수 있는 최대 기간 |
| `ranking.ledger.cleanup.batch-size` | `100` | 한 트랜잭션의 최대 삭제 수, 허용 범위 `1..1000` |
| `ranking.ledger.cleanup.fixed-delay` | `1h` | 이전 실행 종료 뒤 다음 실행까지의 지연 |

Kafka와 DLT의 값은 자동 추정값이 아니라 배포 시 topic의 `retention.ms`와 broker default를 Admin API 또는 `kafka-configs.sh --describe`로 확인해 effective 값으로 설정합니다. compact/delete 조합이나 topic override를 포함해 값을 알 수 없으면 cleanup을 활성화하지 않습니다. 모든 기간과 주기는 양수여야 하고 다음 계약을 만족해야 합니다.

```text
ledger retention >= max(kafka retention, DLT retention, maximum rebuild recovery window)
redis marker TTL >= ledger retention
```

누락, 해석 실패, 범위 위반은 조용히 보정하지 않고 애플리케이션 설정 오류로 실패합니다. scheduler tick도 삭제 SQL 전에 같은 계약을 다시 검증하므로 검증 실패 시 DB ledger와 Redis marker를 0건 변경합니다. 더 긴 Kafka·DLT·rebuild window가 필요하면 ledger retention과 marker TTL을 함께 늘립니다.

marker TTL은 Redis 적용 시점부터, DB retention은 더 늦은 `committed_at`부터 계산됩니다. 동일 기간이면 marker가 먼저 만료될 수 있지만 그 구간에는 아직 `COMMITTED` ledger가 남아 normal/DLT/rebuild 중복 반영을 차단합니다. ledger가 삭제 가능한 시점에는 Kafka·DLT·rebuild 보호 기간도 끝나야 하므로 두 보호 수단이 동시에 사라진 채 재처리 가능한 틈이 없습니다.

삭제 대상은 `state=COMMITTED`, `committed_at < cutoff`, 독립 event 또는 `COMPLETED` rebuild event뿐입니다. `RESERVED`, `REDIS_APPLIED`, 미완료·복구 필요 rebuild event는 삭제하지 않습니다. 후보는 `(state, committed_at)` 인덱스와 `FOR UPDATE SKIP LOCKED`로 고정하고 삭제 SQL에서 predicate를 다시 확인합니다. 여러 인스턴스가 경합하면 어떤 tick은 0건으로 종료할 수 있으며 다음 주기에 재시도합니다.

운영 점검은 MySQL `EXPLAIN`에서 `idx_ranking_event_ledger_cleanup`의 `range` 접근을 확인하고 scheduler의 `ranking_ledger_cleanup_completed deleted=<n>` 로그에서 `deleted <= batch-size`를 확인합니다. 이 job은 Redis key를 `SCAN`하거나 marker를 직접 일괄 삭제하지 않습니다. marker는 설정된 TTL로만 자연 만료됩니다.

## Maintenance ranking rebuild

1. 일반 애플리케이션을 모두 중지하고 `ranking-consumer-group`의 active member가 없는 상태를 만듭니다.
2. MySQL·Redis·Kafka만 실행한 뒤 다음 명령으로 maintenance runner를 시작합니다.

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=local --ranking.consumer.enabled=false --ranking.rebuild.maintenance=true --ranking.rebuild.enabled=true"
```

Windows(PowerShell) 기준입니다. macOS·Linux는 `./gradlew bootRun --args="..."`로 실행합니다.

3. runner는 snapshot과 partition별 end offset을 고정하고 earliest부터 exclusive end까지 replay합니다.
4. `[snapshot-7일, snapshot)`의 `PAID` 주문 DB 집계와 replay가 날짜·menuId별로 같을 때만 Lua로 대상 날짜 key를 원자 교체합니다.
5. 성공 뒤 maintenance 앱을 종료하고 일반 앱을 다시 시작합니다. 이후 event는 이동된 `ranking-consumer-group` offset부터 처리됩니다.
6. active consumer, lock, retention, malformed event, timeout, DB mismatch가 발생하면 일반 앱 재개 전에 원인을 해결합니다. 실패 시 기존 live key와 정상 group offset은 유지되고 temp key는 삭제됩니다.

무중단 online rebuild, Redis Cluster/Sentinel, DB 재집계와 동시 실행은 범위가 아닙니다.

## DLT 확인

Consumer는 한 메시지의 최초 처리가 실패하면 1초 간격으로 2회 재시도합니다. 세 번째 처리까지 실패하면 원본 partition을 유지해 `order.completed.DLT`로 이동합니다.

1. Kafka UI `http://localhost:18080`을 엽니다.
2. `coffee-order-local` cluster에서 `order.completed.DLT` topic을 선택합니다.
3. 메시지 key와 payload를 확인합니다.
4. header에서 원본 topic, partition, offset과 exception class/message를 확인합니다.
5. 아래 원인 표에 따라 데이터, 코드 또는 Redis 상태를 수정합니다.
6. 운영자가 승인한 메시지만 원본 topic으로 수동 재발행합니다. 자동 replay API는 MVP 범위가 아닙니다.

CLI로 원문을 확인하려면 프로젝트 Kafka가 실행 중인 상태에서 다음 명령을 사용합니다.

```bash
docker compose -f docker/compose.yaml exec kafka \
  /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server kafka:9092 \
  --topic order.completed.DLT \
  --from-beginning \
  --property print.key=true \
  --property print.headers=true \
  --property key.separator=' | '
```

관찰을 마치면 이 프로젝트가 시작한 리소스만 정리합니다.

```bash
docker compose -f docker/compose.yaml --profile tools down -v
```

기존 host 3306 MySQL과 `rag-pgvector` 등 다른 프로젝트 컨테이너는 중지하거나 삭제하지 않습니다.

## DLT 재처리 기준

| 원인 | 처리 |
| --- | --- |
| 일시적 Redis 장애 | Redis 정상화 후 선택 메시지만 재발행합니다. |
| Consumer 코드 버그 | 코드 수정과 배포 후 재발행합니다. |
| 잘못된 payload | 원본 메시지를 그대로 재발행하지 않습니다. 보정 가능 여부를 먼저 판단합니다. |
| 이미 처리된 이벤트 | 공통 `ranking_event_ledger`의 같은 fingerprint는 재발행 후 consumer에서 no-op이며, 다른 fingerprint는 fail-closed 합니다. |

재처리는 운영자가 승인한 메시지만 대상으로 합니다. 자동 전체 재처리는 MVP 범위에서 제외합니다.

## DLT 선택 재발행

운영자는 DLT topic의 **한 partition/offset**과 승인자·사유를 확인한 뒤에만 아래 script를 실행합니다. offset 범위나 전체 topic 재처리는 지원하지 않습니다.

```powershell
.\scripts\replay_dlt_message.ps1 -Partition 0 -Offset 12 -ApprovedBy operator-a -Reason "Redis recovered"
```

script는 `order.completed.DLT` record를 해당 offset에서 재조회하고 original topic이 `order.completed`이며 original partition·offset header가 있는지 확인합니다. 불일치면 fail-closed입니다. payload와 key만 보존하며 DLT original·exception·stacktrace header는 복사하지 않고 JSON type header와 내부 `DLT_REPLAY` source header를 추가합니다. 재발행 전 공통 `ranking_event_ledger`에 eventId와 fingerprint를 예약하며 결과는 `REPUBLISHED`입니다. 이미 `COMMITTED`인 같은 fingerprint는 consumer에서 no-op이고 다른 fingerprint는 fail-closed 하므로 `processed_event` 사전 조회에 의존하지 않습니다.

## Redis 랭킹 재구성 후보

1. Redis 랭킹 key가 유실되었거나 오염되었는지 확인합니다.
2. 중복 rebuild 경로가 동시에 실행되지 않게 합니다.
3. 영향받은 key를 초기화합니다.
4. `ranking-rebuild-group`을 실행합니다.
5. 최근 7일 이벤트만 필터링합니다.
6. ZSET을 재구성합니다.
7. 인기 메뉴 API를 검증합니다.

## Redis rebuild 점검표

- Kafka topic retention이 최근 7일 이벤트를 포함하는지 확인합니다.
- rebuild 대상 key 목록을 기록합니다.
- 일반 Consumer가 같은 key를 동시에 갱신하지 않는지 확인합니다.
- rebuild 완료 후 Redis Top 3와 DB 주문 집계 Top 3를 비교합니다.
- 결과와 명령을 `docs/testing/evidence/issue-{number}/verification.md`에 남깁니다. 전역 뷰가 필요하면 [Evidence Guide](../testing/evidence-guide.md)의 재현 명령을 사용하며 생성 파일은 커밋하지 않습니다.
