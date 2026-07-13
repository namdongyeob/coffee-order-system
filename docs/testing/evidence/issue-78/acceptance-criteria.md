# Issue #78 Acceptance Criteria

Issue: #78
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/78

Execution mode: STRICT
Execution mode reason: harness와 Review·QA·Docs workflow policy를 변경하므로 독립 Review, QA, Docs, CI가 필요합니다.
Level 5 required: NO
Level 5 reason: production runtime과 인프라 연결을 변경하지 않습니다.
Level 6 required: NO
Level 6 reason: HTTP/API/UI 계약을 변경하지 않습니다.

## 완료 기준

- [x] 최소 역할 packet, post-QA HEAD 규칙, 검증 소유권, 범위 밖 flaky 안전 정지를 정책·규칙·template과 12개 계약 테스트에 반영했습니다.
- [x] production/runtime, Gradle/build/CI workflow와 애플리케이션 테스트 suite는 변경하지 않았습니다.
- [x] Dev focused 12개 신규 계약과 전체 harness, repository gate, `git diff --check` 실행 결과를 evidence에 기록합니다.
- [x] pilot 전 비교 기준과 실제 수집 가능한 지표를 metrics에 기록합니다.

## 제외 범위

- coffee-order-system production, API, DB schema, Kafka·Redis runtime 동작.
- Gradle/build 설정, GitHub Actions workflow, required CI check와 애플리케이션 테스트 suite.
- GitHub branch protection, secrets, repository settings와 #51·#52·#56의 고유 범위.

## 불변 검증 결과

- 12개 신규 계약은 Issue #78의 네 가지 경량화와 12개 계약을 기계적으로 고정합니다.
- 전체 Python harness와 static repository gate는 policy·evidence 문서와 허용된 scripts 경로만 확인합니다.
- Review, QA, CI, 현재 head와 merge 가능 상태는 GitHub를 정본으로 확인하며 repository evidence에 snapshot으로 복제하지 않습니다.
