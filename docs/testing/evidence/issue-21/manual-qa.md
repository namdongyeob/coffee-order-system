# Issue #21 Manual QA

## Level 3 실제 MySQL 관찰

- Testcontainers MySQL 8.4.5에서 같은 `userId`에 10개 worker를 latch로 동시에 출발시켰습니다.
- 기존 잔액 1,000P에 100P 충전 10건은 응답 10건이 모두 성공하고 최종 잔액 2,000P를 보존했습니다.
- row가 없는 사용자에게 100P 최초 충전 10건은 응답 10건이 모두 성공하고 단일 row의 최종 잔액 1,000P를 보존했습니다.

## 정책 관찰

- 기존 구현의 최초 생성 경쟁은 실제 MySQL에서 lock acquisition failure로 실패했습니다.
- unique 제약 충돌 또는 lock acquisition 실패만 호출자의 트랜잭션과 독립된 `REQUIRES_NEW` 트랜잭션에서 최대 3회 재조회·재시도하며, 그 밖의 DB 오류를 성공으로 바꾸거나 추정하지 않습니다.
- 실제 MySQL에서 상위 트랜잭션을 rollback해도 내부 충전 500P가 commit되어 시도 경계가 독립적임을 확인했습니다.

## 독립 QA 결과

- focused Level 3 6건과 관련 회귀 15건이 통과했습니다.
- 전체 회귀 첫 실행의 Kafka startup timeout을 환경 실패로 분리한 뒤 코드 변경 없이 한 번만 fresh rerun했고, 전체 51건의 failures, errors, skipped가 모두 0임을 확인했습니다.
- 기능 결함은 발견하지 않았습니다. QA FAIL 1건은 자동 close 문구를 사용한 PR-body metadata 결함이었고, 포인트 충전 코드나 테스트 결함으로 분류하지 않습니다.

## 미실행 범위

- Level 5 로컬 애플리케이션 기동과 Level 6 HTTP 요청은 실행하지 않았습니다. runtime 설정과 HTTP 계약은 변경하지 않았으며 실제 DB 동시성은 Level 3으로 검증했습니다.
- 주문, Redisson, k6 검증은 이 Issue 범위가 아닙니다.
