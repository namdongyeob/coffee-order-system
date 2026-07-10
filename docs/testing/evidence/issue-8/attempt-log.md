# Issue Attempt Log

Issue: #8
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/8
Branch: codex/issue-8-kafka-order-event

## Attempt 1

### Generate

- payload record, Kafka publisher, 주문 커밋 이후 발행 연결, JSON producer 설정과 테스트를 TDD로 구현했습니다.

### Evaluate

- PASS. focused unit, Level 4 Kafka, Level 5 기동, Level 6 HTTP, 전체 회귀가 통과했습니다.
- Level 6의 별도 Kafka payload CLI 관찰은 PARTIAL입니다.

### Failure Cause

- 첫 Level 4 실행에서 Java time module 누락으로 serialization이 실패했습니다.
- module 추가 뒤 `orderedAt`이 배열로 직렬화되어 문서의 문자열 계약과 달랐습니다.

### Change Scope

- Jackson Java time 지원과 `orderedAt` 문자열 shape만 production 범위에서 보완했습니다.
- 테스트 harness API 컴파일 오류는 테스트 파일 안에서만 수정했습니다.

### Reverification

- focused + Level 4 `BUILD SUCCESSFUL in 1m 23s`.
- Level 5 앱 기동 PASS, Level 6 HTTP 200/201 PASS, Kafka CLI payload PARTIAL.
- 전체 회귀 `BUILD SUCCESSFUL in 1m 26s`.

### Next Attempt

- 독립 Review와 QA를 실행합니다. QA가 Level 5/6 결과를 확정하면 Docs Agent가 `verification-log.md`에 옮긴 뒤 pre-push hook, push, draft PR 생성을 재실행합니다.
