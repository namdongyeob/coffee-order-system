# Issue 개발 흐름

모든 작업은 `Specify -> Clarify -> Plan -> Implement -> Verify -> Review -> Document` 중 현재 단계를 먼저 밝힙니다.

1. Issue에서 목표, 포함 범위, 제외 범위, Acceptance Criteria를 확인합니다.
2. Level 5와 Level 6 필요 여부와 이유를 `acceptance-criteria.md`에 기록합니다.
3. 정책이 비어 있으면 Clarify에서 멈추고 질문 Issue 또는 ADR 초안을 만듭니다.
4. Dev Agent가 승인된 Issue 하나를 구현하고 focused test를 실행합니다.
5. 구현 완료 후 Review Agent와 QA Agent를 병렬 배정합니다. Review는 diff를 검토하고 QA는 필요한 독립 테스트와 실제 환경 검증을 실행합니다.
6. FAIL 결과는 Main Coordinator가 원래 Dev Agent에게 그대로 반환하며, Main은 직접 수정하거나 리뷰하지 않습니다.
7. Review와 QA가 PASS하면 Docs Agent가 evidence, 검증 로그, 관련 계약 문서를 갱신합니다.
8. GitHub Actions가 컴파일과 전체 테스트를 최종 기계 판정합니다.
9. Main Coordinator는 필수 보고서와 CI 상태만 확인해 `READY_FOR_HUMAN`, `FAIL`, `BLOCKED` 중 하나로 표시합니다.
10. 사람이 PR merge와 Issue close를 결정합니다.

역할과 쓰기 권한은 `docs/ai/orchestration-policy.md`, 검증 기준은 `docs/testing/test-strategy.md`, evidence 형식은 `docs/testing/evidence-guide.md`를 따릅니다.
