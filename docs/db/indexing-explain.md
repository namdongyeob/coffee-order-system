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