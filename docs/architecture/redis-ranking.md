# Redis 랭킹

## Key 설계

```text
popular:menus:2026-07-09
popular:menus:2026-07-08
```

## 쓰기

```text
ZINCRBY popular:menus:{date} 1 {menuId}
```

## 읽기

1. 최근 7일의 일별 key를 선택합니다.
2. 점수를 임시 key로 합산합니다.
3. 점수 내림차순으로 Top 3를 조회합니다.

## 주의 사항

- Redis는 원천 데이터가 아닙니다.
- Redis 랭킹이 유실되어도 주문 데이터가 삭제되는 것은 아닙니다.
- Kafka replay 기반 rebuild는 도전 기능입니다.