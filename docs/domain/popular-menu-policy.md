# 인기 메뉴 정책

## Redis 랭킹

- Key 형식은 `popular:menus:{yyyy-MM-dd}`입니다.
- Member는 문자열 형태의 `menuId`입니다.
- Score는 주문 횟수입니다.
- 증가는 `ZINCRBY key 1 menuId` 방식입니다.

## 최근 7일

- 오늘을 포함한 최근 7일의 날짜별 key를 사용합니다.
- Redis 7.4의 저장 없는 `ZUNION`으로 점수를 합산합니다. `ZUNIONSTORE` 임시 key, TTL, 명시적 삭제는 사용하지 않습니다.
- `ZUNION`은 임시 key 수명 관리와 정리 실패 위험을 만들지 않는 대신 결과 전체를 반환합니다. 이 서비스의 고정 seed 메뉴 범위에서는 전체 결과를 애플리케이션에서 정렬하는 비용을 허용합니다.
- 최종 순위는 주문 횟수 내림차순, 동점이면 `menuId`의 **숫자 오름차순**입니다. Redis ZSET member의 문자열 사전순에 의존하지 않으므로 같은 score의 `2`는 `10`보다 앞섭니다.
- Redis member가 현재 DB에 없는 삭제 메뉴를 가리키면 그 항목을 건너뜁니다. 삭제 메뉴를 제외한 나머지 순위에서 최대 Top 3를 채웁니다.

## 복구

- Redis 랭킹은 파생 데이터라 재구성할 수 있습니다.
- 1순위 도전 복구 전략은 `ranking-rebuild-group`을 사용하는 Kafka replay입니다.
- DB 재집계는 보조 복구 후보입니다.
- Kafka replay와 DB 재집계를 동시에 실행하면 score가 중복 증가할 수 있으므로 동시에 실행하지 않습니다.
