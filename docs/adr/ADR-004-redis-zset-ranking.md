# ADR-004 Redis ZSET 랭킹

## 상태

Accepted.

## 결정

인기 메뉴 랭킹은 Redis ZSET 기반 파생 데이터로 관리합니다.

## 이유

Redis ZSET은 score 기반 Top N 조회에 적합합니다. 최근 7일 조회는 날짜별 key와 ZSET union으로 처리합니다.