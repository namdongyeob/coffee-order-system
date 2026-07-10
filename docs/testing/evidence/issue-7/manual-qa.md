# Issue #7 Manual QA

## Level 5 로컬 앱·인프라

- PASS. MySQL 8.4.10과 Redis 7.4.2를 사용했고 Flyway migration 4개가 적용됐습니다.
- Redisson은 `127.0.0.1:16379`에 연결됐고 애플리케이션은 14.961초에 기동했습니다.
- health 요청은 HTTP 200과 `UP`을 반환했습니다.

## Level 6 실제 HTTP·DB·락 경합

- 포인트 충전은 HTTP 200이었습니다.
- 정상 주문은 HTTP 201, 169ms였고 DB 잔액은 10000에서 5500으로 감소했으며 `PAID` 주문 1건이 생성됐습니다.
- 동일 사용자 락을 선점한 상태의 주문은 HTTP 409, `ORDER_LOCK_NOT_ACQUIRED`, 2066ms였고 DB는 변경되지 않았습니다.
- 락 선점 상태에서 보낸 동시 2개 주문은 각각 HTTP 409, 2064ms와 2071ms였고 DB는 변경되지 않았습니다.
- QA가 MySQL, Redis, 애플리케이션 리소스와 포트를 정리했습니다.

## Gate 상태

- 독립 Review: PASS. findings 없이 `APPROVED`입니다.
- 독립 QA: PASS. Level 1, 4, 5, 6을 확인했습니다.
- CI: remote branch와 PR이 아직 없어 pending입니다.
