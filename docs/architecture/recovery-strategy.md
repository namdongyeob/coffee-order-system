# 복구 전략

## Issue #14 maintenance rebuild 계약

- 일반 `ranking-consumer-group`의 활성 member가 없는 maintenance window에서만 실행합니다.
- Redis의 `ranking:rebuild:lock`을 획득한 runner 한 개만 실행합니다.
- 시작 시 `Asia/Seoul` snapshot과 partition별 exclusive end offset을 고정합니다. `ranking-rebuild-group`의 committed offset은 무시하고 earliest부터 읽습니다.
- 집계 범위는 `[snapshot - 7일, snapshot)`입니다. 이 범위와 교차하는 모든 날짜 key를 같은 temp/live 집합으로 다루므로 snapshot이 자정이 아니면 경계 날짜를 포함해 최대 8개가 될 수 있습니다.
- replay와 같은 범위의 `PAID` 주문을 날짜·menuId별로 정확히 비교합니다. retention 유실, malformed event, timeout, 불일치는 fail-closed입니다.
- 검증 전에는 실행별 temp namespace만 갱신합니다. 검증 성공 뒤 Lua 한 번으로 기존 live를 backup하고 temp를 교체합니다.
- Redis 교체 뒤에만 정상 consumer group offset을 캡처한 end로 이동합니다. offset 이동 실패 시 live key를 복원하고 temp를 삭제합니다.
- 매번 새 snapshot과 namespace를 사용하며 일반 `processed_event` 이력을 사용하지 않습니다.

## Redis 랭킹 유실

Redis 랭킹 데이터는 파생 데이터입니다. 복구 후보는 다음과 같습니다.

1. `ranking-rebuild-group`을 사용하는 Kafka replay.
2. DB 주문 원천 데이터 기반 재집계.

## 추천 우선순위

Kafka 로그와 Consumer Group 개념을 적용할 수 있으므로 Kafka replay를 1순위 도전 기능으로 둡니다.

## 전제 조건

- `order.completed` topic retention은 최소 7일보다 길게 둡니다.
- rebuild 실행 중 일반 랭킹 Consumer와 rebuild Consumer가 같은 Redis key를 동시에 증가시키지 않게 합니다.
- Kafka replay와 DB 재집계를 동시에 실행하지 않습니다.
- rebuild 시작 전에 대상 Redis key를 명시적으로 백업하거나 삭제합니다.
- rebuild 결과는 API 응답과 DB 원천 주문 수를 비교해 검증합니다.

## Redis rebuild 흐름

```text
1. 필요하면 일반 랭킹 쓰기를 중지하거나 격리합니다.
2. 영향받은 `popular:menus:*` key를 초기화합니다.
3. `ranking-rebuild-group`을 실행합니다.
4. `order.completed`를 읽습니다.
5. 최근 7일 이벤트만 필터링합니다.
6. Redis ZSET을 재구성합니다.
7. 인기 메뉴 API 결과를 검증합니다.
```

## DLT 복구

DLT 이동은 자동으로 처리합니다. MVP에서 DLT 재처리는 수동 또는 스크립트 기반으로 다룹니다.

실무 기준으로는 완전 자동 재처리보다 수동 승인 또는 반자동 스크립트가 안전합니다. DLT에는 데이터 오류, 코드 버그, 외부 인프라 장애가 섞일 수 있으므로 원인 분류 없이 자동으로 원본 topic에 되돌리면 같은 실패를 반복하거나 잘못된 이벤트를 중복 반영할 수 있습니다.

MVP 결정은 다음과 같습니다.

| 항목 | 결정 |
| --- | --- |
| DLT 이동 | 구현합니다. |
| DLT 조회 | Kafka UI 또는 CLI 기준으로 운영 문서화합니다. |
| DLT 재처리 | MVP에서는 공개 API를 만들지 않습니다. |
| 도전 기능 | 검증된 메시지만 원본 topic에 재발행하는 스크립트를 둡니다. |
