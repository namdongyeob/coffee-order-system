# Manual QA

- 대상 DB는 MySQL 8.4.5 Testcontainers입니다.
- `OrderRepositoryQuerydslIntegrationTest`가 기간 내 메뉴별 주문 수 4, 3, 2를 QueryDSL 결과로 확인했고, 기간 종료 시각과 같은 주문 10건은 `[from, to)` 범위 밖으로 제외했습니다.
- 같은 테스트가 아래 SQL의 `EXPLAIN`을 실행했습니다.

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

- 관찰된 plan은 `type=index`, `key=fk_orders_menu`, `rows=10`, `Extra=Using where; Using temporary; Using filesort`입니다.
- Redis 인기 메뉴 API, 로컬 HTTP 기동, 실제 HTTP 요청은 Issue 범위 밖이므로 실행하지 않았습니다.
