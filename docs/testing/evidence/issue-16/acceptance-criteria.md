# Issue #16 완료 기준

Issue: #16
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/16

Execution mode: STRICT
Execution mode reason: QueryDSL 의존성, DB 원천 검증 조회, MySQL EXPLAIN 근거를 함께 변경하므로 독립 Review와 QA가 필요합니다.
Level 5 required: NO
Level 5 reason: HTTP API나 로컬 애플리케이션 실행 경로를 추가하거나 변경하지 않았습니다.
Level 6 required: NO
Level 6 reason: HTTP API 계약을 추가하거나 변경하지 않았습니다.

- [x] `PAID` 주문의 기간별 메뉴 집계 Top 3 DB 원천 조회를 QueryDSL repository fragment로 구현했습니다.
- [x] QueryDSL 의존성과 Q 타입 annotation processor 설정을 추가했습니다.
- [x] MySQL Testcontainers에서 repository 결과와 `EXPLAIN`을 Level 3으로 검증했습니다.
- [x] `docs/db/indexing-explain.md`에 실제 plan과 인덱스 후보 비교·DDL 보류 결정을 기록했습니다.
- [x] focused Level 3 및 전체 Level 1 회귀 smoke를 실행했습니다.
