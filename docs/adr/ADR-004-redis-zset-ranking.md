# ADR-004 Redis ZSET 랭킹

## 상태와 결정일

Accepted. 결정일: 2026-07-09.

## 맥락과 문제

인기 메뉴 API는 최근 7일 주문 수 기준 Top 3을 조회해야 합니다. 주문 원천 데이터와 조회 최적화용 랭킹 데이터를 분리해야 합니다.

## 결정 동인

- score 기반 Top N을 빠르게 조회합니다.
- 날짜별 집계로 최근 7일 범위를 구성합니다.
- Redis 유실이 주문 원천 데이터 유실로 이어지지 않게 합니다.

## 검토한 선택지

| 선택지 | 판단 |
| --- | --- |
| Redis ZSET과 일별 key union | 채택. [Redis 랭킹 설계](../architecture/redis-ranking.md)의 `ZINCRBY`와 최근 7일 Top 3 흐름에 맞습니다. |
| 주문 DB에서 매 요청 집계 | 보류. 정확한 원천 조회 후보이나 인기 조회의 반복 집계 비용 근거를 더 확인해야 합니다. |
| 단일 누적 ZSET | 제외. 최근 7일 경계를 직접 보장하지 못합니다. |

## 결정과 이유

인기 메뉴 랭킹은 Redis ZSET 기반 파생 데이터로 관리하고, 날짜별 key와 ZSET union으로 최근 7일 Top 3을 조회합니다.

## 결과와 단점

Top N 조회를 원천 주문 집계와 분리할 수 있습니다. 반면 Redis 랭킹은 원천이 아니므로 소비 지연·유실 시 일시적으로 오래된 결과가 보일 수 있고, 복구 절차가 필요합니다.

## 재검토 조건

- 7일 집계 비용이나 Redis key 관리가 허용 범위를 넘을 때.
- 랭킹 정확도·복구 요구가 변경될 때.

## 관련 항목

- 관련 Issue: [#28](https://github.com/namdongyeob/coffee-order-system/issues/28).
- 설계: [Redis 랭킹 설계](../architecture/redis-ranking.md), [인기 메뉴 정책](../domain/popular-menu-policy.md).
- 대체·폐기 규칙: [ADR 운영 규칙](README.md)을 따릅니다.
