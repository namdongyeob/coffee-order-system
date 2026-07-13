# 인덱싱과 EXPLAIN

## 원칙

감으로 인덱스를 추가하지 않습니다.

## 흐름

1. 쿼리를 정의합니다.
2. 생성 SQL을 확인합니다.
3. `EXPLAIN`을 실행합니다.
4. `type`, `key`, `rows`, `Extra`를 비교합니다.
5. 적용 전후 판단 근거를 기록합니다.

## 후보 쿼리

```sql
EXPLAIN
SELECT menu_id, COUNT(*)
FROM orders
WHERE status = 'PAID'
  AND ordered_at >= ?
GROUP BY menu_id
ORDER BY COUNT(*) DESC
LIMIT 3;
```

## Issue #16 QueryDSL 검증 조회

`OrderRepository.findTopPaidMenuOrderCounts(from, to)`는 Redis 인기 메뉴 API를 대체하지 않는 DB 원천 검증 조회입니다. 기간은 `from` 이상, `to` 미만이며 `PAID` 주문만 메뉴별 건수로 집계해 상위 3건을 반환합니다.

```sql
SELECT menu_id, COUNT(*) AS order_count
FROM orders
WHERE status = 'PAID'
  AND ordered_at >= ?
  AND ordered_at < ?
GROUP BY menu_id
ORDER BY COUNT(*) DESC
LIMIT 3;
```

## MySQL 8.4 EXPLAIN 관찰

`OrderRepositoryQuerydslIntegrationTest`는 MySQL 8.4.5 Testcontainers에 기간 내 `PAID` 주문 10건을 넣고 위 SQL의 `EXPLAIN`을 실행했습니다. 2026-07-13 관찰 결과는 다음과 같습니다.

| 항목 | 관찰값 |
| --- | --- |
| `type` | `index` |
| `possible_keys` | `fk_orders_menu`, `idx_orders_status_ordered_at`, `idx_orders_ordered_at_menu_id` |
| `key` | `fk_orders_menu` |
| `rows` | `10` |
| `Extra` | `Using where; Using temporary; Using filesort` |

작은 검증 데이터에서는 MySQL이 상태·기간 인덱스가 아니라 `menu_id` 외래 키 인덱스를 선택했습니다. 따라서 `idx_orders_status_ordered_at`의 효과나 새 인덱스의 성능 향상을 이 결과만으로 주장하지 않습니다.

## 인덱스 후보 비교와 결정

| 후보 | 조회 조건과의 관계 | 관찰·비용 | 결정 |
| --- | --- | --- | --- |
| 기존 `idx_orders_status_ordered_at (status, ordered_at)` | `status =`와 기간 range에 직접 대응합니다. | 현재 10행 계획에서는 선택되지 않았고, 집계·`COUNT(*)` 정렬의 temporary/filesort도 제거하지 못했습니다. | 유지합니다. |
| 기존 `idx_orders_ordered_at_menu_id (ordered_at, menu_id)` | 기간 range에는 대응하지만 `status`가 선행 열이 아닙니다. | `possible_keys`에는 있으나 현재 계획의 선택 key가 아닙니다. | 유지합니다. |
| 후보 `(status, ordered_at, menu_id)` | 조건과 select/group 대상까지 포함하는 covering 후보입니다. | `GROUP BY menu_id`와 `ORDER BY COUNT(*) DESC` 때문에 temporary/filesort 제거를 보장하지 않으며, 기존 인덱스와 중복되는 쓰기 비용이 생깁니다. | 실제 운영 규모 데이터의 `EXPLAIN ANALYZE` 전에는 추가하지 않습니다. |

다음 재검토에서는 실제 기간 분포와 행 수로 `EXPLAIN ANALYZE`를 실행해 `rows`, 실제 시간, temporary/filesort 여부를 기존 인덱스와 후보 인덱스에서 비교합니다. 그 근거가 있을 때만 별도 migration Issue로 DDL을 제안합니다.
