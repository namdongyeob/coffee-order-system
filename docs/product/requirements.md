# 요구사항

## 과제 요약

다수 서버와 다수 인스턴스 환경에서도 안정적으로 동작하는 커피숍 주문 시스템을 구현합니다.

## 필수 API

| API | 요구사항 |
| --- | --- |
| 커피 메뉴 목록 조회 | 메뉴 ID, 이름, 가격을 조회합니다. |
| 포인트 충전 | 사용자 식별값과 충전 금액을 받아 포인트를 충전합니다. 1원은 1P입니다. |
| 커피 주문/결제 | 사용자 식별값과 메뉴 ID를 받아 주문하고 포인트를 차감합니다. |
| 인기 메뉴 목록 조회 | 최근 7일간 주문 수 기준 인기 메뉴 3개를 조회합니다. |

## 도전 요구사항

- 다수 서버 인스턴스를 고려합니다.
- 동시성 이슈를 고려합니다.
- 데이터 일관성을 보장합니다.
- 각 기능과 제약사항에 대한 테스트를 작성합니다.

## MVP 범위

- 메뉴 조회 API.
- 포인트 충전 API.
- 주문/결제 API.
- 인기 메뉴 Top 3 API.
- DB 트랜잭션과 비관적 락 기반 포인트 정합성 보장.
- Redisson 기반 주문/결제 진입 제어.
- Kafka `OrderCompletedEvent` 발행.
- Redis ZSET 랭킹 Consumer.
- Consumer 멱등 처리와 DLT 이동.
- Transactional Outbox 기반 Kafka 발행 재시도.
- Kafka replay 기반 ranking rebuild와 운영자 승인 DLT 선택 재발행.
- DB `ranking_event_ledger`와 Redis Lua marker를 통한 normal consumer·DLT·rebuild 양방향 중복 집계 방지.
- 30일 ranking ledger bounded retention과 미완료 복구 상태 보존.
- 테스트 전략, Issue evidence, 실제 Docker·HTTP·k6 검증.

## MVP 제외 범위

- WebSocket/STOMP.
- Redis Cluster.
- Kubernetes.
- 완전 자동 DLT 재처리.
- 복잡한 관리자 API.

## 완료 근거

- API와 오류 계약: [API 명세](../api/api-spec.md)
- DB 최종 구조: [ERD와 Flyway V1~V7](../db/erd.md)
- Kafka·Redis 복구 불변조건: [ADR-008](../adr/ADR-008-ranking-recovery-ledger.md)
- 실제 Level 5·6와 k6: [Issue #114 evidence](../testing/evidence/issue-114/verification.md)
