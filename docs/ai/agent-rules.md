# Issue 개발 흐름

모든 작업은 `Specify -> Clarify -> Plan -> Implement -> Verify -> Review -> Document` 중 현재 단계를 먼저 밝힙니다.

1. Issue에서 목표, 포함 범위, 제외 범위, Acceptance Criteria를 확인합니다.
2. Level 5와 Level 6 필요 여부와 이유를 `acceptance-criteria.md`에 기록합니다.
3. 정책이 비어 있으면 Clarify에서 멈추고 질문 Issue 또는 ADR 초안을 만듭니다.
4. Dev Agent가 승인된 Issue 하나를 구현하고 focused test를 실행합니다.
5. Main Agent가 최종 focused test와 전체 smoke test를 재실행합니다.
6. Review와 QA 결과를 Dev Agent에게 반환하고 수정 후 다시 검토합니다.
7. evidence, 검증 로그, 관련 계약 문서를 갱신합니다.
8. 사람이 PR merge와 Issue close를 결정합니다.

역할과 쓰기 권한은 `docs/ai/orchestration-policy.md`, 검증 기준은 `docs/testing/test-strategy.md`, evidence 형식은 `docs/testing/evidence-guide.md`를 따릅니다.
