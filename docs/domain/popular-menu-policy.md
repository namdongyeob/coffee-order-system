# 인기 메뉴 정책

## Redis 랭킹

- Key 형식은 `popular:menus:{yyyy-MM-dd}`입니다.
- Member는 문자열 형태의 `menuId`입니다.
- Score는 주문 횟수입니다.
- 증가는 `ZINCRBY key 1 menuId` 방식입니다.

## 최근 7일

- 오늘을 포함한 최근 7일의 날짜별 key를 사용합니다.
- Redis ZSET union으로 점수를 합산합니다.
- 점수 내림차순으로 Top 3를 반환합니다.

## 복구

- Redis 랭킹은 파생 데이터라 재구성할 수 있습니다.
- 1순위 도전 복구 전략은 `ranking-rebuild-group`을 사용하는 Kafka replay입니다.
- DB 재집계는 보조 복구 후보입니다.
- Kafka replay와 DB 재집계를 동시에 실행하면 score가 중복 증가할 수 있으므로 동시에 실행하지 않습니다.