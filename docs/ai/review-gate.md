# Review Gate

Review Agent는 `STRICT` 작업에서 읽기 전용으로 변경 diff와 Issue 요구사항을 검토합니다. 역할과 쓰기 금지는 [오케스트레이션 정책](orchestration-policy.md)을, 검증 Level과 실행 소유권은 [테스트 전략](../testing/test-strategy.md)을 따릅니다.

## 우선 검토 순서

1. Issue 요구사항, 제외 범위, Acceptance Criteria의 누락 여부를 확인합니다.
2. 변경된 동작에 맞는 테스트와 회귀 방지 근거가 있는지 확인합니다.
3. 포인트·주문 정합성, Kafka 중복 처리, Redis 랭킹 중복 증가처럼 변경 영역의 회귀 위험을 확인합니다.
4. Generic Manager, 숨은 공통 계층, 요구사항과 무관한 리팩터링 같은 과한 추상화와 범위 초과를 확인합니다.
5. 산출물이 프로젝트 근거 없는 일반론, 가짜 검증 주장, 이유 없는 한영 혼합 이름, 동작을 설명하지 못하는 테스트명을 포함하지 않는지 확인합니다.

## 판정과 반환

- 요구사항 누락, 회귀 위험, 테스트 누락, 과한 추상화, 문서 품질 문제가 있으면 근거와 영향 범위를 기록해 Dev Agent에게 반환합니다.
- 현재 Issue 범위 밖이거나 정책 결정이 필요한 문제는 직접 수정하지 않고 Follow-up Issue 후보로 기록합니다.
- Review Agent는 production, test, docs 파일을 수정하거나 테스트를 재실행하지 않습니다.

구현 방식의 Dev 전용 기준은 [구현 가드레일](implementation-guardrails.md)에서 확인합니다.
