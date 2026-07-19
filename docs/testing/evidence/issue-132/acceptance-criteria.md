# Issue #132 Acceptance Criteria

Issue: #132
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/132

Execution mode: STRICT
Execution mode reason: Kafka normal consumer, rebuild offset, Redis swap과 다중 인스턴스 동시성 경계를 함께 변경합니다.
Level 5 required: YES
Level 5 reason: 실제 MySQL·Kafka·Redis와 별도 normal consumer·rebuild runner 프로세스에서 fencing 동작을 확인해야 합니다.
Level 6 required: NO
Level 6 reason: 직접 변경되는 HTTP API 계약이 없습니다.

## Dev 완료 기준

- [x] normal consumer는 공용 recovery lock을 획득한 뒤에만 ledger와 Redis를 변경합니다.
- [x] rebuild가 lock을 보유하면 consumer는 record를 DLT로 보내지 않고 container pause 기반으로 같은 offset을 재시도합니다.
- [x] rebuild는 destructive swap 직전에 normal group member를 다시 확인하고 late join을 감지하면 prepared run을 취소한 뒤 fail-closed 합니다.
- [x] shared fence token에 owner 종류와 rebuild runId 또는 consumer event/attempt를 기록하고, 획득 실패·consumer 차단 로그가 같은 owner를 남깁니다.
- [x] 차단·중단 이유, rebuild runId, Kafka partition/offset을 구조화 로그로 남깁니다.
- [x] `capture E -> offset E 처리 시도 -> swap/offset 이동`의 허용 순서에서 최종 Redis 점수는 정확히 1입니다.
- [x] 동일 eventId의 ledger, marker, Redis score와 consumer offset이 모순되지 않습니다.
- [x] ledger만 `COMMITTED`이고 Redis 점수가 누락되는 상태를 focused 통합 테스트와 실제 환경에서 배제했습니다.
- [x] 기존 normal ledger, DLT, DLT↔rebuild, 중복 eventId와 rebuild recovery focused 회귀가 통과했습니다.
- [x] production 전용 test hook, topic/payload/partition, DLT replay, ranking 정책은 변경하지 않았습니다.
- [x] test-only spy/latch로 processor lock 실패를 결정적으로 관찰하고, release 전 원 offset 미커밋·DLT 0과 consumer-first 순서를 검증합니다.
- [x] Issue #132 기본 evidence와 PR-head CI 전제 검증 경로를 준비했습니다.

## STRICT 후속 게이트

독립 Review·QA와 최신 PR-head GitHub Actions CI는 draft PR 생성 뒤 Main Coordinator가 확인합니다.
