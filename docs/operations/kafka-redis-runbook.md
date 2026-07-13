# Kafka Redis Runbook

## Maintenance ranking rebuild

1. 일반 애플리케이션을 모두 중지하고 `ranking-consumer-group`의 active member가 없는 상태를 만듭니다.
2. MySQL·Redis·Kafka만 실행한 뒤 다음 명령으로 maintenance runner를 시작합니다.

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=local --ranking.consumer.enabled=false --ranking.rebuild.maintenance=true --ranking.rebuild.enabled=true"
```

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
| 이미 처리된 이벤트 | `processed_event`를 확인하고 재발행하지 않습니다. |

재처리는 운영자가 승인한 메시지만 대상으로 합니다. 자동 전체 재처리는 MVP 범위에서 제외합니다.

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
