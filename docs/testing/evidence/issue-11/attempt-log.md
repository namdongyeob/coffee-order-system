# Issue #11 Attempt Log

Issue: #11
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/11
Branch: codex/issue-11-kafka-retry-dlt-v2

## Attempt 1

### Generate

- 시작 시각: `2026-07-12T15:26:30.0273613+09:00`.
- Kafka error handler와 실제 DLT 이동을 먼저 실패하는 Testcontainers 테스트로 고정합니다.

### Evaluate

- RED 1: observer consumer 설정 인자 순서 오류로 consumer 생성이 실패했습니다.
- RED 2: Spring Kafka 4.1 기본 destination `order.completed-dlt`가 문서 정본 `order.completed.DLT`와 달라 DLT observer가 메시지를 찾지 못했습니다.
- GREEN: 명시적 destination resolver 뒤 실제 Kafka에서 최초 처리와 2회 재시도, `.DLT` 이동 계약이 통과했습니다.

### Failure Cause

- 첫 RED는 테스트 설정 결함이었습니다. 두 번째 RED는 library 기본 suffix에 의존한 구현 결함이었습니다.

### Change Scope

- Kafka consumer error handler 설정, DLT 통합 테스트, Kafka runbook과 Issue #11 evidence만 변경합니다.

### Reverification

- focused DLT 1 test PASS, 관련 Kafka 3 classes PASS, 전체 48 tests PASS입니다.
- local profile 앱, Redis 장애, 실제 Kafka DLT key/header/payload 관찰과 cleanup을 완료했습니다.
- 종료 시각: `2026-07-12T15:49:30.3025334+09:00`.

### Next Attempt

- 없음.
