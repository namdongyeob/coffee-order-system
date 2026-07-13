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
11. fresh Review·QA와 metadata 불일치가 있을 때만 호출하는 Docs에는 Issue URL, worktree 경로, base/head SHA, Acceptance Criteria, 서로 다른 실제 필수 정본 문서 3~5개 경로, diff 범위, 직전 P0/P1 finding만 전달합니다. 이 allowlist 밖 packet key는 허용하지 않고 문서 경로는 `AGENTS.md`, `docs/ai/*.md`, `docs/testing/*.md`, `.codex/skills/*/SKILL.md`의 canonical repository-relative 경로만 사용합니다. source 본문·전체 conversation·별도 source snapshot을 전달하거나 저장소에 만들지 않으며 역할은 worktree와 GitHub 정본을 직접 읽습니다.
12. 고정 자율 Issue 큐의 순서는 `Dev 구현·focused 검증과 evidence·PR body preflight -> fresh Review -> independent QA -> 최신 CI -> merge·close`입니다. QA 뒤 repository HEAD가 같으면 Docs commit과 두 번째 전체 Review를 만들지 않습니다. GitHub-only 상태 갱신은 repository commit을 요구하지 않으며, production·test·build·runtime·workflow·API 또는 도메인 정책 문서 변경은 Review와 필요한 QA를 stale 처리합니다.
13. Dev는 focused 검증, QA는 Dev와 중복되지 않는 실제 미검증 위험, `quality-gates`는 전체 Level 1 회귀를 소유합니다. broad-risk 변경의 Dev 전체 회귀 예외와 current diff 관련 실패의 flaky 금지는 `docs/ai/orchestration-policy.md`를 따릅니다.
14. 범위 밖 flaky는 clean process 1회 격리 PASS와 current head CI PASS가 함께 있을 때만 후보로 기록합니다. 격리 FAIL은 test-only blocker의 원인 진단·조건 기반 동기화 1회까지만 허용하며, production 변경 필요·원인 불명·안정화 실패 또는 무변경 `BLOCKED` wake-up은 안전 정지합니다.

역할과 쓰기 권한은 `docs/ai/orchestration-policy.md`, 검증 기준은 `docs/testing/test-strategy.md`, evidence 형식은 `docs/testing/evidence-guide.md`를 따릅니다.
