# Issue 개발 흐름

모든 작업은 `Specify -> Clarify -> Plan -> Implement -> Verify -> Review -> Document` 중 현재 단계를 먼저 밝힙니다.

1. Issue에서 목표, 포함 범위, 제외 범위, Acceptance Criteria와 `Execution mode` 및 reason을 확인합니다.
2. Level 5와 Level 6 필요 여부와 이유를 `acceptance-criteria.md`에 기록합니다.
3. 정책이 비어 있으면 Clarify에서 멈추고 질문 Issue 또는 ADR 초안을 만듭니다.
4. 실행 모드별 역할 구성은 `docs/ai/orchestration-policy.md`의 실행 모드 표만 따릅니다.
5. 검증 레벨과 실행 소유권은 `docs/testing/test-strategy.md`를 따릅니다.
6. 문서 반영은 실행 모드 표에 지정된 경우에만 확정된 결과를 기록합니다.
7. `STANDARD`와 `STRICT`의 FAIL은 Main Coordinator가 Skill의 제한된 재시도 패킷으로 원래 Dev Agent에게 반환합니다. Main은 직접 수정하거나 리뷰하지 않습니다.
8. source 이벤트만 고정 `quality-gates` required check를 만들고 PR body `edited`는 별도 `metadata-gates` check와 concurrency를 사용해 source run을 취소·대체하지 못합니다. `ready_for_review`는 동일 SHA source run을 다시 만들지 않습니다. #137 bootstrap의 source `quality-gates`는 전환 전 계약대로 전체 Gradle을 실행하고, #138부터 단일 영향도 분류기가 `requires_java_ci=true`로 판정한 source/test/build/runtime 변경에서만 전체 Gradle을 실행합니다.
9. draft PR은 독립 검증과 CI를 시작하기 위한 중간 상태이며 완료가 아닙니다. Main Coordinator는 `docs/ai/orchestration-policy.md`가 정한 모드별 필수 독립 검증 보고와 CI PASS를 모두 확인한 뒤에만 `READY_FOR_HUMAN`으로 표시하며, pending 상태를 `READY_FOR_HUMAN`으로 표시하지 않습니다.
10. 고정 자율 Issue 큐 실험 밖에서는 사람이 PR merge와 Issue close를 결정합니다. merge 거버넌스 기본값은 `docs/ai/orchestration-policy.md`, 실험의 적용 저장소·고정 큐·bootstrap 경계·조건부 merge·close·안전 정지 상세는 `docs/ai/autonomous-queue-runbook.md`를 따릅니다.
11. Dev·Review·QA·Docs·Combined Verifier는 `fork_turns="none"`으로 시작합니다. packet은 Issue URL, worktree, base/head SHA, Acceptance Criteria, 허용 쓰기 범위, 직접 관련 canonical 정본 1~5개 경로, diff 범위, focused 검증, 직전 P0/P1 또는 마지막 실패 하나, `SUBAGENT-STOP: superpowers:using-superpowers`, `summary-only` 출력 예산만 허용합니다. source 본문·전체 conversation·전체 tool/test log·전체 PR conversation은 전달하거나 별도 snapshot으로 저장하지 않습니다.
11-a. Java·Gradle 작업 packet은 ASCII worktree를 사용하고 `worktree_path_action`을 통과해야 합니다. non-ASCII 경로는 `BLOCKED: NON_ASCII_WORKTREE_PATH`입니다. Agent assignment는 `heartbeat`와 `deadline`을 기록하며 `RUNNING`·`STALLED`·`TIMEOUT` 상태를 사용합니다. 동일 실패의 자동 retry는 `RETRY_ONCE` 한 번으로 제한하고 다음 실패는 `BLOCKED: RETRY_LIMIT`입니다.
12. 고정 자율 Issue 큐의 순서는 `Dev 구현·focused 검증과 evidence·PR body preflight -> fresh Review -> independent QA -> 최신 CI -> merge·close`입니다. preflight는 기본 Acceptance Criteria·verification과 존재하는 optional Attempt·metrics의 disposition·head·retry 모순을 fail-closed로 발견합니다. execution-head 이후 delta는 `git diff --name-status --find-renames`의 `ChangeRecord`를 유지하며 current-Issue evidence의 A/M만 허용하고 R/D는 stale로 차단합니다. QA 뒤 evidence·PR metadata·raw artifact만 바뀌면 source-tree SHA에 묶인 판정을 재사용합니다.
13. Dev는 RED와 최종 focused 검증, QA는 Dev와 중복되지 않는 실제 미검증 위험, `quality-gates`는 분류기가 요구한 전체 Level 1 회귀를 소유합니다. Review는 테스트를 실행하지 않습니다. broad-risk 변경의 Dev 전체 회귀 예외와 current diff 관련 실패의 flaky 금지는 `docs/ai/orchestration-policy.md`를 따릅니다.
14. 범위 밖 flaky는 clean process 1회 격리 PASS와 current head CI PASS가 함께 있을 때만 후보로 기록합니다. 격리 FAIL은 test-only blocker의 원인 진단·조건 기반 동기화 1회까지만 허용하며, production 변경 필요·원인 불명·안정화 실패 또는 무변경 `BLOCKED` wake-up은 안전 정지합니다.
15. Agent 진행은 `wait_agent` 또는 완료 알림으로 기다립니다. timeout 또는 명시적 stall 의심 때만 process·git·docker 진단 snapshot을 한 번 허용하고 상태 변화 없는 snapshot을 반복하지 않습니다. 장기 명령의 session/cell handle은 새 명령 대신 이어받습니다.
16. 비싼 Gradle·Docker·Level 3~7 명령은 source/test/runtime 입력, 정규화 명령, 환경 profile이 같고 PASS가 있으면 재사용합니다. 입력 변경, 이전 FAIL 진단, flaky 격리, 분류기 stale, 독립 QA 최종 증명만 재실행하며 근거를 `verification.md` 또는 실패한 `attempt-log.md`에 남깁니다.
17. 조건부 auto-merge는 mode와 무관하게 서로 다른 Writer·Review·QA가 같은 source-tree SHA에 남긴 최종 `APPROVED`·`PASS`, 그 SHA의 고정 source check `quality-gates: SUCCESS`를 요구합니다. 같은 SHA의 `metadata-gates: SUCCESS`는 이를 대체하지 못합니다. 역할·판정·SHA가 누락되거나 FAIL·BLOCKED·stale이면 안전 정지합니다.

역할과 쓰기 권한은 `docs/ai/orchestration-policy.md`, 검증 기준은 `docs/testing/test-strategy.md`, evidence 형식은 `docs/testing/evidence-guide.md`를 따릅니다.
