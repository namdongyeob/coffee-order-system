# Kafka Redis Runbook

## DLT 확인

1. Kafka UI를 엽니다.
2. `order.completed.DLT`를 확인합니다.
3. 실패 원인을 파악합니다.
4. 데이터 또는 코드를 수정합니다.
5. 선택한 메시지를 수동 또는 스크립트로 재발행합니다.

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
- 결과와 명령을 `docs/testing/verification-log.md`에 남깁니다.
