# 자율 Issue 큐 runbook (이 저장소 한정 조건부)

이 문서는 핵심 실행 계약이 아니며 `namdongyeob/coffee-order-system` 연습 저장소의 자율 Issue 큐 실험을 운영할 때만 읽습니다. 기본 merge 거버넌스(사람 도메인 오너 최종 승인)와 역할·검증의 정본은 [오케스트레이션 정책](orchestration-policy.md)입니다. 이 실험은 최종 팀 프로젝트로 이전하지 않습니다.

## 고정 자율 Issue 큐 실험

이 절은 `namdongyeob/coffee-order-system`만 적용합니다. 기본 모드는 여전히 사람이 PR merge와 Issue close를 결정하며, 아래 고정 큐에서만 Main Coordinator가 조건부 merge와 close를 운영할 수 있습니다. 이는 프로젝트 정책과 Main Coordinator의 운영 결정이며 GitHub branch protection 또는 ruleset 변경이 아닙니다.

- 적용 큐는 `#61 -> #45 -> #55 -> #11 -> #21 -> #12 -> #13 -> #14 -> #15 -> #16 -> #51 -> #52 -> #53 -> #54 -> #56 -> #57 -> #58 -> #36`입니다.
- 한 번에 Issue 하나와 production/test 작성자 한 명만 허용합니다.
- Issue #60 PR은 자동 merge 또는 close하지 않으며 사람이 직접 merge합니다.
- #61은 Issue #60 PR이 사람에 의해 merge된 뒤에만 시작합니다. #61은 재현 가능한 로컬 실행의 P1 blocker이지만 이 PR과 분리된 Issue로 처리합니다.
- #45는 #61이 완료된 뒤에만 시작합니다.
- Issue #36이 merge·close되거나 사용자가 중단을 선언하면 즉시 만료됩니다.
- 최종 팀 프로젝트에는 자동 이전하지 않습니다.

### 역할과 Review 수정 루프

Dev Agent는 Issue별 worktree에서 production/test의 유일한 작성자입니다. Reviewer는 Dev의 전체 대화를 상속하지 않은 fresh context에서 Issue 본문, 이 정책 정본, base/head SHA, diff만 입력받아 읽기 전용으로 검토합니다. Reviewer의 판정은 `APPROVED`, `REVISE`, `BLOCKED` 중 하나이며 구현자의 self-review는 독립 Review가 아닙니다. QA는 Dev·Reviewer와 분리하고 production/test/docs를 수정하지 않습니다. Docs는 Dev evidence와 정본 사이의 metadata 불일치가 확인된 경우에만 분리해 호출하며 확정된 결과만 반영합니다.

1. Dev는 구현, TDD, focused verification과 필요한 전체 회귀를 수행하고 draft PR을 생성합니다.
2. CI와 fresh Reviewer가 현재 head SHA를 검증합니다.
3. `REVISE`이면 Coordinator는 P0/P1과 현재 Issue 범위의 수정만 원래 Dev에게 한 번만 반환합니다.
4. Dev는 같은 PR에서 수정, 관련 테스트, push를 수행합니다.
5. fresh Reviewer가 전체 최종 diff를 재검토합니다.
6. 두 번째 `REVISE`이면 자동 루프를 중단하고 사용자에게 보고합니다. `BLOCKED`, 정책 미결정 또는 Issue 범위를 넘는 수정도 같은 안전 정지를 적용합니다.
7. Reviewer는 production/test를 수정하지 않으며 별도 구현자가 같은 파일을 수정하지 않습니다.

### 조건부 merge와 다음 Issue

Main Coordinator는 다음 조건을 모두 충족할 때만 해당 PR을 merge하고 연결 Issue를 close할 수 있습니다.

- Issue의 측정 가능한 완료 기준을 모두 충족합니다.
- 필수 Dev verification이 PASS입니다.
- fresh Reviewer 최종 판정이 `APPROVED`입니다.
- 독립 QA가 필요한 검증 Level을 `PASS`로 판정했습니다.
- evidence와 실제 역할 보고·명령·수치가 일치하며, Docs Agent를 호출했다면 그 반영도 일치합니다.
- required CI checks가 최신 head SHA에서 모두 PASS입니다.
- Review가 확인한 head SHA와 merge 직전 head SHA가 같습니다.
- PR base가 `main`이고 최신 `origin/main` 기준 merge 가능하며 conflict가 없습니다.
- 범위 밖 변경, 비밀값, 개인정보, 내부 평가 자료 노출이 없습니다.
- branch protection, required check, review 또는 hook을 우회하지 않습니다.
- force push, 관리자 우회, check 무시 merge를 사용하지 않습니다.

merge 뒤 실제 merge commit과 Issue close 상태를 확인한 뒤에만 다음 Issue를 최신 `origin/main`의 새 worktree에서 시작합니다. 이 실험의 자동 merge·close 조건은 bootstrap 경계 이전의 Issue #60 PR에는 적용하지 않습니다.

### 새 Issue와 안전 정지

- 현재 변경이 만든 버그, 완료 기준 누락, 테스트 누락은 같은 PR에서 수정합니다.
- 기존 코드의 별도 결함, 범위 밖 리팩터링, 성능 개선, 새 정책은 현재 PR에 섞지 않습니다.
- 중복 Issue를 먼저 검색한 뒤 후속 Issue 후보를 작성합니다.
- 현재 큐를 막는 P0/P1 결함은 Issue를 생성하고 큐 앞에 삽입할 수 있습니다.
- 비차단 개선은 backlog Issue로만 생성하고 현재 승인 큐를 자동 확장하지 않습니다.
- 정책 결정이 필요한 새 Issue는 생성 후 자동 구현하지 않고 사용자에게 보고합니다.

다음 중 하나라도 발생하면 merge, close, 다음 Issue 진행을 중단하고 현재 상태와 마지막 안전한 commit/PR을 사용자에게 보고합니다.

- 같은 Review 수정의 두 번째 실패.
- 요구사항 또는 정책의 복수 해석.
- 원인을 분류하지 못한 CI 실패 또는 반복 flaky.
- merge conflict 또는 예상하지 못한 최신 main 변경.
- 외부 서비스, 비밀값, 추가 권한, GitHub 설정 변경 필요.
- schema, security, 결제, 동시성 또는 이벤트 계약에서 Issue 본문 밖의 결정 필요.
- Reviewer와 Dev의 기술적 판단 충돌이 evidence로 해소되지 않음.
- required check·branch protection·GitHub API 상태를 확실히 확인할 수 없음.

Issue #36 종료 뒤 Main Coordinator는 실험 종료를 사용자에게 알리고, 처리 Issue와 PR 수, Review 판정·안전 정지, merge 성공·실패, CI·QA 결함, 범위 밖 후속 Issue, 사람 개입, 작업 시간과 절감 근거를 evidence 또는 후속 회고 Issue에 보존합니다. 자동 merge 조항의 cleanup 후보와 팀 이전 여부를 검토하는 transfer 후보는 사용자에게 제시할 뿐, 자동 실행하지 않습니다.
