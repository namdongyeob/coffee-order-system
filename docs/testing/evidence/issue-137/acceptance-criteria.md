# Issue #137 Acceptance Criteria

Issue: #137
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/137

Execution mode: STRICT
Execution mode reason: harness, workflow, 역할 packet, stale와 merge 전 검증 의미를 변경하므로 독립 Review·QA·최신 CI가 필요합니다.
Level 5 required: NO
Level 5 reason: 애플리케이션 runtime과 외부 인프라 연결을 변경하지 않습니다.
Level 6 required: NO
Level 6 reason: HTTP API 계약과 실제 요청 경로를 변경하지 않습니다.

## 완료 기준

- [x] 자동 역할은 `fork_turns="none"`과 직접 관련 정본 1~5개의 최소 packet을 사용하고 source·전체 대화·전체 log를 복제하지 않습니다.
- [x] 네 영향도 출력은 name-status 기반 단일 fail-closed 분류기와 하나의 테스트 표에서 관리됩니다.
- [x] README/evidence-only, source/test, migration/build/runtime, workflow/harness/policy, API/domain/architecture, mixed, unknown, rename/delete를 검증했습니다.
- [x] 선언 mode가 계산된 floor보다 낮으면 gate가 실패합니다.
- [x] 조건부 auto-merge는 mode와 무관하게 서로 다른 Writer·Review·QA의 최종 판정과 같은 source-tree SHA의 required CI를 요구합니다.
- [x] Review·QA 누락, `REVISE`·`FAIL`·`BLOCKED`, stale head와 CI head 불일치를 차단합니다.
- [x] PR `edited`와 docs/evidence-only는 Gradle을 반복하지 않고 source/test/build/runtime은 최종 source SHA에서 Gradle을 유지합니다.
- [x] workflow의 링크 검사는 한 번이고 Gradle compile/test는 `test` 한 invocation으로 합쳤으며 고정 required job 이름을 유지합니다.
- [x] metrics를 후속 Issue 완료 gate에서 제거하고 기본 evidence를 Acceptance Criteria와 verification으로 축소했습니다.
- [x] #137 자체는 기존 evidence 6종과 preflight·fresh Review·QA·최신 CI 계약을 유지합니다.
- [x] source-tree SHA와 runtime evidence를 연결하고 evidence·PR metadata·raw artifact만의 변경은 runtime evidence를 유지합니다.
- [x] evidence와 verification table, changed path와 링크 입력은 한 gate 실행에서 읽은 값을 재사용합니다.
- [x] wait/notification, active handle 이어받기, 비싼 동일 입력 명령 재사용과 허용된 재실행 사유를 기계 계약과 정본에 반영했습니다.
- [x] `SUBAGENT-STOP: superpowers:using-superpowers`와 summary-only 출력 예산을 역할 packet에 강제합니다.
- [x] focused contract tests와 전체 scripts suite가 PASS했고 범위 밖 production·test·DB·runtime 변경이 없습니다.

## 후속 게이트

fresh read-only Review, independent QA와 최신 PR-head GitHub Actions CI는 draft PR 생성 뒤 Main Coordinator가 확인합니다.
