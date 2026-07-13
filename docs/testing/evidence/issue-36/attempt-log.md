# Issue Attempt Log

Issue: #36
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/36
Branch: claude/issue-36-doc-lifecycle-audit

Current disposition: PASS
Current Attempt: 2
Current head: f8756fb

## Attempt 1

### Generate

- 시작 시각은 기록하지 못해 `미측정`입니다.
- Issue #36의 두 번째 댓글은 "이 Issue는 개정 라운드 마지막(#58 완료 후)에 실행합니다"라는 선행조건을 명시했습니다. 저장소 소유자는 세션 대화에서 "#57·#58은 팀 프로젝트로 넘어가서 실사용 문제가 생기면 그때 처리하고, 그때 맞춰 문서 구조를 다시 손보면 된다"는 근거로 이 선행조건을 의도적으로 해제하고 지금 #36을 진행하기로 결정했습니다. #57·#58 자체는 이번 세션에서 별도로 계속 보류(미구현)합니다.
- `docs/` non-evidence 문서 54개 전체를 `find`로 수집하고, 각 문서의 basename/경로가 다른 어떤 문서(`docs/`, `README.md`, `AGENTS.md`, `CLAUDE.md`, `.codex/`, `.github/`)에 인용되는지 grep으로 확인했습니다.
- 참조 0건으로 나온 문서(`k6-plan.md`, `postman-guide.md`, `troubleshooting-log.md`, `questions-for-tutor.md`, `verification-matrix.md`, `k6-results.md`, `ADR-001`, `ADR-006`, `archive/5-6-orchestration-implementation-plan.md`)와 참조가 애매한 문서(`subagent-workflow.md`, `lazycodex-runbook.md`)는 내용을 직접 읽고 개별 판단했습니다.
- `erd.md`, `lecture-mapping.md`, `overview.md`는 AI hot path에서는 미참조였지만 실제로는 `README.md`(사람 온보딩 색인)에서 링크되고 있어 obsolete가 아님을 확인했습니다. 이는 세션 초반 사용자와의 대화에서 이미 한 번 발견한 패턴(erd.md 오탐)과 같은 종류의 사례입니다.
- `7ee1180`: `docs/ai/doc-lifecycle.md`를 새로 작성해 active(약 32개, root 진입점 포함) / conditional(약 20개) / archive(evidence 디렉터리 일괄 + 6개 개별 항목) / obsolete 후보(4개, 2개는 Issue #88과 중복 등록되지 않도록 "이미 등록" 표기) 표를 작성했습니다. `docs/ai/rule-source-map.md`에 `doc-lifecycle.md`를 정본으로 등록하는 행을 추가했습니다.

### Evaluate

- PASS. `python -m pytest scripts/tests/test_harness_gate.py`가 head `7ee1180`에서 107건(110 subtests) PASS입니다(#54의 인코딩 수정 이후 카운트와 동일, 이번 Issue는 하네스 코드를 변경하지 않았습니다).

### Failure Cause

- RED: `docs/` 문서가 실제 Agent 진입점(Router)·정본 지도·evidence guide에서 도달 가능한지 감사된 적이 없어, 오래된/중복/미사용 문서와 실제 필요한 문서가 섞여 있었습니다.

### Change Scope

- `docs/ai/doc-lifecycle.md`: 신규 작성(분류 결과).
- `docs/ai/rule-source-map.md`: `doc-lifecycle.md`를 정본 지도에 등록하는 1행 추가.
- 애플리케이션 코드, harness 스크립트, 다른 정본 문서의 규칙 내용은 변경하지 않았습니다.

### Reverification

- `python -m pytest scripts/tests/test_harness_gate.py`는 head `7ee1180`에서 107건(110 subtests) PASS입니다.
- `python scripts/harness_gate.py --issue 36 --branch claude/issue-36-doc-lifecycle-audit --base-ref 7d32e2d --check-links --include-worktree`는 결과를 `commands.md`에 기록합니다.
- Level 5/6은 NO입니다. 문서 변경만 있어 runtime/API 테스트는 실행하지 않았습니다.
- 종료 시각은 기록하지 못해 `미측정`입니다.

### Next Attempt

- fresh 독립 Combined Verifier 결과를 반영합니다. 사용자 요청에 따라 Combined Verifier가 PASS/APPROVED를 반환할 때까지 이 절차를 반복하되, **merge는 하지 않고** 승인된 상태에서 PR만 남깁니다.

## Attempt 2

### Generate

- fresh 독립 Combined Verifier가 head `1fe372e`에서 `REVISE`를 반환했습니다. 지적 사항: `docs/adr/README.md`가 non-evidence 54개 인벤토리 중 유일하게 분류표에서 빠져 있었습니다(완료 기준 1번 "docs 전체 인벤토리와 분류 결과가 있습니다"를 53/54로 불완전하게 충족). Verifier는 12개 이상의 다른 분류 근거를 직접 grep·읽기로 재검증했고 모두 정확하다고 확인했습니다.
- `f8756fb`: `docs/ai/doc-lifecycle.md`의 conditional 표에 `docs/adr/README.md` 행을 추가했습니다. `docs/adr/README.md`는 ADR 상태 표기·Superseded 절차를 정의하는 운영 규칙 문서이지만 Router·정본 지도 어디서도 참조되지 않고, 과거 evidence(issue-28)에서만 언급됨을 확인해 conditional + 도달성 문제로 분류했습니다. "Router·정본 지도 도달성 문제 요약"과 "후속 조치 후보" 절에도 반영했습니다.

### Evaluate

- PASS. Combined Verifier가 반환한 유일한 REVISE 지적을 원래 Dev 범위에서 한 번에 정정했습니다. 안전 불변조건 약화(P0)는 없었습니다.

### Failure Cause

- RED: Attempt 1에서 `docs/adr/README.md`를 `commands.md`의 검증 명령(`grep -n "ADR-001\|ADR-006" docs/adr/README.md`)에 사용했지만 그 결과를 분류표 행으로 옮기지 않고 누락했습니다.

### Change Scope

- `docs/ai/doc-lifecycle.md`: `docs/adr/README.md` 분류 행 1개와 관련 요약 2곳만 추가했습니다. 다른 분류 결과는 변경하지 않았습니다.

### Reverification

- `python -m pytest scripts/tests/test_harness_gate.py`는 head `f8756fb`에서 107건(110 subtests) PASS입니다.
- 인벤토리 완전성 재확인: `find docs -type f -name "*.md" ! -path "docs/testing/evidence/*"`로 수집한 54개 원본 파일이 모두 `doc-lifecycle.md`에 언급되는지 프로그램으로 대조해 누락 0건을 확인했습니다.
- `python scripts/harness_gate.py --issue 36 --branch claude/issue-36-doc-lifecycle-audit --base-ref 7d32e2d --check-links --include-worktree`는 head `f8756fb`에서 PASS입니다.
- Level 5/6은 NO입니다. 문서 변경만 있어 runtime/API 테스트는 실행하지 않았습니다.
- 종료 시각은 기록하지 못해 `미측정`입니다.

### Next Attempt

- 없음. evidence를 확정하고 push 뒤 fresh 독립 Combined Verifier 재검토를 받습니다. 사용자 지시대로 APPROVED/PASS를 받으면 거기서 멈추고 **merge하지 않습니다.**

## Attempt 2 최종 결과

- fresh 독립 Combined Verifier는 head `4efe837`(base~head 전체 diff 기준)에서 `APPROVED`입니다. 이전 REVISE 지적(`docs/adr/README.md` 분류 누락)이 실제로 해소됐는지 재확인했고, 인벤토리 완전성(54개 원본 파일 전부 언급)을 프로그램으로 재검증했고, 이전과 다른 6개 분류 근거를 새로 spot-check해 모두 정확함을 확인했습니다. 삭제된 파일이 없음을 `git diff --stat`으로 확인했고, evidence 파일 내부 정합성(Attempt 2/head `f8756fb` 일치, 재시도 수 1)도 확인했습니다. 새 결함은 없습니다.
- 수행 시각은 기록되지 않아 `미측정`입니다.
- 남은 절차: 사용자 지시에 따라 **merge는 하지 않습니다.** push된 head `4efe837`의 GitHub Actions `quality-gates` CI 결과만 확인하고, PR을 draft에서 ready로 전환한 뒤 사용자에게 보고합니다.
