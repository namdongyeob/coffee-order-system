# Issue Attempt Log

Issue: #102
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/102
Branch: claude/issue-102-readme-rewrite
Current disposition: PASS
Current Attempt: 1
Current head: 224b3232583f315348009a74ccf073ab8ac71e81

## Attempt 1

### Generate

기존 README.md(문서 허브 스타일, 각 정책·API 상세는 다른 문서로 링크만)를 완결형 프로젝트 소개 문서로 재작성했다. 참고 스타일은 사용자가 제시한 외부 저장소 README(`usersy628/coffee-order-system`)의 구조(설계 의도, 기술 선택 표, 패키지 구조, 정책, API 명세)를 차용하되, 그 저장소가 아직 미구현 상태(README 자체가 "다음 구현 대상은..."이라고 명시)인 것과 달리, 이 저장소는 필수 API 4개와 Outbox까지 전부 구현·테스트 완료된 상태이므로 미래형 문구 없이 완료형으로 작성했다. 실제 `build.gradle`, migration 파일, 컨트롤러·DTO, `ErrorCode.java`, `docker/compose.yaml`, `docs/operations/local-runbook.md`를 직접 읽고 사실 확인한 내용만 반영했다.

### Evaluate

PASS. 독립 검증 agent가 10개 항목을 대조해 9개 정확, 1개(테스트 개수)는 agent 자신의 grep 오탐으로 판명(직접 재확인으로 반증). 링크 검사(`harness_gate.py --links-only`)도 PASS.

### Failure Cause

없음. 독립 검증 agent가 "CHANGES NEEDED"로 보고한 테스트 개수 불일치는, agent가 사용한 `grep -c "@Test"`가 `@TestConfiguration`, `@Testcontainers`, `@TestMethodOrder`를 부분 문자열로 함께 카운트해 82로 과다 집계한 것이었다. 정확한 패턴(`grep -E "^\s*@Test(\(.*\))?\s*$"`)으로 재확인한 결과 76건이었고, 이는 직전 전체 회귀 실행(Issue #99 merge 직후)의 JUnit XML 집계 76건과 정확히 일치해 README의 원래 서술이 맞았음을 확인했다. README는 수정하지 않았다.

### Change Scope

`README.md` 1개 파일만 수정했다.

### Reverification

- `python scripts/harness_gate.py --links-only --base-ref main --include-worktree` — PASS.
- 독립 검증 agent(general-purpose, fresh)가 tech stack, 패키지 구조, API DTO, 오류 코드, 정책 수치, migration 목록, 로컬 실행 포트, 문서 링크 14개, 톤(미래형 문구 여부)을 실제 파일과 대조 — 9/10 정확, 1건은 재검증으로 반증(위 Failure Cause 참고).
- `grep -rE "^\s*@Test(\(.*\))?\s*$" src/test --include=*.java | wc -l` — 76(README 수치와 일치, 직전 전체 회귀 JUnit XML 집계와도 일치).

### Next Attempt

없음.
