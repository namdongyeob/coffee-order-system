# 하네스 지표와 최종 프로젝트 이전 (조건부 참조)

이 문서는 핵심 실행 계약이 아니며 지표 집계와 팀 프로젝트 이전 준비 단계에서만 읽습니다. 실행 모드·역할·검증의 정본은 [오케스트레이션 정책](orchestration-policy.md)과 [테스트 전략](../testing/test-strategy.md)입니다.

## 품질 개선 지표

최종 프로젝트로 가져갈 때 에이전트 수가 아니라 아래 결과를 비교합니다.

- Issue 범위 밖 변경 파일 수.
- Review에서 발견된 요구사항·회귀·과한 추상화 건수.
- Mock 통과 후 실제 API 검증에서 추가로 발견된 결함 수.
- 실패를 재현할 수 있는 테스트와 evidence의 비율.
- PR 문서와 실제 코드·측정 수치의 불일치 건수.
- 같은 원인으로 재발한 `agent-mistakes.md` 항목 수.

이 수치는 [Issue metrics template](../testing/evidence/issue-metrics-template.md)의 고정 형식으로 `docs/testing/evidence/issue-N/metrics.md`에 남깁니다. 값이 없으면 추정하지 않고 `0`, `없음`, `미측정` 중 해당하는 값을 쓰며 근거를 함께 적습니다. 이 기록은 최종 프로젝트에서 현재 방식과 개선된 방식을 비교하는 기준입니다.

## 최종 프로젝트 이전 범위

그대로 이전할 항목은 전역 작업 규칙, 단일 작성자, 읽기 전용 Review/QA, 검증 레벨, evidence, 사람의 merge 승인입니다. 커피 주문 도메인의 Redisson 키, Kafka topic, Redis ZSET 정책은 이전하지 않고 최종 프로젝트의 도메인 문서와 ADR로 다시 결정합니다.

이 저장소 한정 자율 Issue 큐 실험과 조건부 auto-merge는 최종 팀 프로젝트로 이전하지 않으며, 팀 저장소는 사람 도메인 오너 최종 승인 기본값을 사용합니다. 자율 큐 실험의 상세 규칙은 별도 조건부 runbook에서 관리합니다.
