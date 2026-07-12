# Issue 개발 흐름

모든 작업은 `Specify -> Clarify -> Plan -> Implement -> Verify -> Review -> Document` 중 현재 단계를 먼저 밝힙니다.

1. Issue에서 목표, 포함 범위, 제외 범위, Acceptance Criteria와 `Execution mode` 및 reason을 확인합니다.
2. Level 5와 Level 6 필요 여부와 이유를 `acceptance-criteria.md`에 기록합니다.
3. 정책이 비어 있으면 Clarify에서 멈추고 질문 Issue 또는 ADR 초안을 만듭니다.
4. 실행 모드별 역할 구성은 `docs/ai/orchestration-policy.md`의 실행 모드 표만 따릅니다.
5. 검증 레벨과 실행 소유권은 `docs/testing/test-strategy.md`를 따릅니다.
6. 문서 반영은 실행 모드 표에 지정된 경우에만 확정된 결과를 기록합니다.
7. `STANDARD`와 `STRICT`의 FAIL은 Main Coordinator가 Skill의 제한된 재시도 패킷으로 원래 Dev Agent에게 반환합니다. Main은 직접 수정하거나 리뷰하지 않습니다.
8. GitHub Actions가 `STANDARD`와 `STRICT`의 컴파일과 전체 테스트를 최종 기계 판정합니다.
9. draft PR은 독립 검증과 CI를 시작하기 위한 중간 상태이며 완료가 아닙니다. Main Coordinator는 `docs/ai/orchestration-policy.md`가 정한 모드별 필수 독립 검증 보고와 CI PASS를 모두 확인한 뒤에만 `READY_FOR_HUMAN`으로 표시하며, pending 상태를 `READY_FOR_HUMAN`으로 표시하지 않습니다.
10. 고정 자율 Issue 큐 실험 밖에서는 사람이 PR merge와 Issue close를 결정합니다. 실험의 적용 저장소, 고정 큐, bootstrap 경계, 조건부 merge·close, 안전 정지는 `docs/ai/orchestration-policy.md`만 따릅니다.
11. 기계적으로 검증 가능한 metadata 불일치는 코드·정책 수정 반환과 구분하고, `docs/ai/orchestration-policy.md`의 고정 allowlist와 별도 recovery budget을 따릅니다. Main Coordinator는 저장소 파일을 직접 수정하지 않습니다.

역할과 쓰기 권한은 `docs/ai/orchestration-policy.md`, 검증 기준은 `docs/testing/test-strategy.md`, evidence 형식은 `docs/testing/evidence-guide.md`를 따릅니다.
