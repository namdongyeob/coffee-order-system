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

## Attempt 2

### Generate

- 시작 시각: `2026-07-12T17:37:12+09:00` (승인된 merge commit 시각).
- 최신 `origin/main` 병합 중 `docs/testing/verification-log.md` 단일 충돌을 사람 승인 범위에서 정합화했습니다.

### Evaluate

- main의 Issue #71 행과 기존 Issue #11 Level 1/4/5 행을 모두 보존했고 unresolved conflict는 0입니다.

### Failure Cause

- Issue #71과 Issue #11이 같은 verification log 끝에 각각 행을 추가해 일반 merge가 단일 content conflict로 중단됐습니다.

### Change Scope

- 충돌 해결은 `docs/testing/verification-log.md` 한 파일로 제한했고 production/test/Kafka 구현은 변경하지 않았습니다.

### Reverification

- 새 merge head에서 focused actual broker test PASS, 관련 Kafka 3-class PASS, 전체 48 tests PASS를 재확인했습니다.
- 종료 시각: `2026-07-12T17:46:01.5355625+09:00`.

### Next Attempt

- 없음.
