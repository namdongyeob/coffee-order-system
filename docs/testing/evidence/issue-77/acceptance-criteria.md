# Issue #77 Acceptance Criteria

Issue: #77
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/77

Execution mode: STRICT
Execution mode reason: Kafka listener retry와 DLT Testcontainers 통합 검증의 timing 안정성을 다루므로 독립 Review, QA, Docs, CI가 모두 필요합니다.
Level 5 required: NO
Level 5 reason: production 코드와 runtime 설정을 변경하지 않고 기존 실제 Kafka Testcontainers Level 4 경로의 테스트 준비 순서만 동기화합니다.
Level 6 required: NO
Level 6 reason: HTTP 또는 API 계약을 변경하거나 실제 HTTP 요청으로 확인할 경로가 없습니다.

## 완료 기준

- [x] 수정 전 실패 근거와 Kafka 단계별 처리 순서를 기록합니다.
- [x] production 변경 없이 condition-based listener assignment를 기다립니다.
- [x] 실제 DLT record의 key, partition, value와 original-topic header 검증을 유지합니다.
- [x] 격리 테스트를 깨끗한 새 Gradle 프로세스에서 3회 독립 실행해 모두 PASS합니다.
- [x] 관련 Kafka/DLT 통합 테스트와 전체 회귀를 실행해 PASS합니다.
- [x] repository gate, `git diff --check`, PR body preflight를 실행합니다.
- [x] 독립 QA가 동일 격리 테스트 3회와 관련 Kafka/DLT 회귀를 재검증했습니다.
- [x] Docs 최종 동기화에서 불변 실행 결과와 cleanup evidence를 대조했습니다.
- [x] Review, QA, CI, 현재 head와 merge 가능 상태는 저장소에 snapshot으로 복제하지 않고 GitHub를 정본으로 확인합니다.

## 제외 범위

- production DLT 정책, retry 횟수, topic과 event 계약 변경.
- 단순 sleep 증가, 무제한 timeout, 무조건 재시도, 테스트 비활성화 또는 assertion 약화.
- Issue #14, PR #76과 Issue #15 변경.

## 불변 검증 결과

- Dev의 test-only 변경과 로컬 Level 1·4 검증은 PASS입니다.
- 독립 QA의 새 Gradle 프로세스 격리 3회와 관련 Kafka/DLT 회귀에서 flaky가 재발하지 않았습니다.
- STRICT 역할 판정과 최신 CI는 GitHub PR comments와 checks에서 확인합니다.
