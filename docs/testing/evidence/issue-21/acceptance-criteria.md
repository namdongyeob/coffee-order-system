# Issue #21 Acceptance Criteria

Issue: #21
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/21
Branch: codex/issue-21-point-concurrency

Execution mode: STRICT
Execution mode reason: 실제 MySQL의 최초 row 생성 경쟁, 비관적 락과 트랜잭션 재시도 정책을 변경하므로 독립 Review, QA, Docs와 CI가 필요합니다.
Level 5 required: NO
Level 5 reason: 애플리케이션 기동 설정이나 인프라 연결은 변경하지 않으며 실제 MySQL 트랜잭션 동작은 Level 3 Testcontainers에서 검증합니다.
Level 6 required: NO
Level 6 reason: HTTP 요청·응답 계약과 validation은 변경하지 않으며 이 Issue의 핵심인 동시 트랜잭션 정합성은 Level 3에서 직접 검증합니다.

## 완료 기준

- 기존 `UserPoint` row에 같은 사용자 충전 10건이 동시에 실행돼도 요청 10건이 성공하고 총 충전액이 보존됩니다.
- row가 없는 사용자의 최초 충전 10건이 동시에 실행돼도 row 하나만 생성되고 요청 10건이 성공하며 총 충전액이 보존됩니다.
- 최초 생성 경쟁의 unique 제약 또는 lock acquisition 실패는 호출자의 트랜잭션과 독립된 `REQUIRES_NEW` 트랜잭션에서 최대 3회 재조회·재시도합니다.
- 주문, Redisson, k6와 다른 Issue 범위는 변경하지 않습니다.

## 최종 충족 결과

- 독립 QA가 실제 MySQL Level 3 focused 6건에서 기존 row, missing row, outer rollback 뒤 독립 commit을 확인했습니다.
- 독립 QA가 관련 회귀 15건과 전체 회귀 51건을 확인했습니다.
- 최초 전체 회귀의 Kafka Testcontainer startup timeout은 제품 assertion 결함이 아닌 환경 실패로 분리했고, 코드 변경 없는 단일 fresh rerun이 통과했습니다.
- Level 5와 Level 6은 위 결정대로 실행하지 않았습니다.
