# ADR-006 QueryDSL과 인덱싱

## 상태

Planned.

## 결정

필요한 DB 검증 조회는 QueryDSL로 작성합니다. 인덱스는 생성 SQL과 EXPLAIN 결과를 기준으로 결정합니다.

## 이유

QueryDSL은 SQL보다 자동으로 빠른 도구가 아니라 타입 안전하게 쿼리를 표현하는 도구입니다. 성능 판단은 DB 실행계획으로 검증해야 합니다.