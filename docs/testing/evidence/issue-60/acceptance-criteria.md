# Issue #60 완료 기준

- [x] `orchestration-policy.md`와 Issue workflow에 현재 저장소 한정의 고정 Issue 큐 `#61 -> #45 -> #55 -> #11 -> #21 -> #12 -> #13 -> #14 -> #15 -> #16 -> #51 -> #52 -> #53 -> #54 -> #56 -> #57 -> #58 -> #36`, 유효 기간, 조건부 자동 merge·close 권한을 명시했습니다.
- [x] Dev와 fresh read-only Reviewer의 컨텍스트·쓰기 권한 분리를 명시했습니다.
- [x] `APPROVED`·`REVISE`·`BLOCKED`, 원래 Dev에 대한 1회 수정·재리뷰, 두 번째 `REVISE` 안전 정지를 명시했습니다.
- [x] merge 전 CI·QA·evidence·head SHA·merge 가능 상태 검사와 우회 금지를 명시했습니다.
- [x] 새 Issue의 중복 검색·P0/P1 큐 삽입·비차단 backlog·정책 Issue 사람 보고 규칙을 명시했습니다.
- [x] #36 종료 시 권한 만료, 실험 결과 보존, cleanup·transfer 후보 제시 규칙을 명시했습니다.
- [x] #60 PR은 자동 merge 또는 Issue close 대상이 아니고 사람이 merge해야 하며, 사람 merge 뒤 자동 큐는 #61부터 시작하고 #45는 #61 완료 뒤에만 시작한다는 bootstrap 경계를 정책 계약 테스트로 고정했습니다.
- [x] 하네스 focused 계약 테스트, 전체 Python 테스트 62건, Issue repository gate, diff check를 PASS했습니다.

Execution mode: STRICT
Execution mode reason: 현재 프로젝트의 Issue 큐, Main Coordinator merge·close 권한, 역할 격리와 workflow policy를 변경하는 bootstrap 작업입니다.
Level 5 required: NO
Level 5 reason: 애플리케이션 런타임과 인프라 실행 계약을 변경하지 않습니다.
Level 6 required: NO
Level 6 reason: HTTP API·실제 요청 흐름을 변경하지 않습니다.

## Bootstrap boundary

- 이 PR은 정책 활성화 전 bootstrap PR이므로 자동 merge하거나 Issue #60을 자동 close하지 않습니다.
- 사람이 이 PR을 merge한 뒤 자동 큐는 #61부터 시작합니다. #45는 #61이 완료된 뒤에만 시작하며, 이 Docs 단계에서는 #61·#45 모두 시작하지 않았습니다.
- #61의 runtime·IntelliJ 구현은 이 PR 범위 밖이며, 별도 Issue에서 처리합니다.
- Reviewer의 P1 수정 후 fresh Review, QA, 최신 CI는 이 Docs evidence commit을 포함한 최신 HEAD에서 다시 확인해야 합니다.
