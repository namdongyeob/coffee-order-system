# Commands

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `git log --oneline --follow -- docs/product/questions-for-tutor.md` | 삭제 후보 실사용 이력 확인 | 최초 커밋 `56d4196` 1건만 존재. 재수정 없음. |
| `git log --oneline --follow -- docs/testing/verification-matrix.md` | 삭제 후보 실사용 이력 확인 | 최초 커밋 `56d4196`과 `e2ec4cb` 2건. |
| `git show e2ec4cb -- docs/testing/verification-matrix.md` | `e2ec4cb`의 실제 변경 내용 확인 | 검증 결과 기록 경로 문구 1줄만 기계적으로 치환됨(내용 갱신 아님). |
| `git log --all --oneline --grep="verification-matrix"` / `--grep="questions-for-tutor"` | 커밋 메시지에서 두 문서가 언급된 적 있는지 확인 | 매치 0건. |
| `grep -rn "questions-for-tutor\|verification-matrix" --include="*.md" .` | 삭제 전 다른 문서에서의 참조 여부 확인 | `docs/ai/doc-lifecycle.md`(Issue #36 obsolete 후보 표)에서만 언급, 다른 정본 문서에서는 0건. |
| `python -m pytest scripts/tests/test_harness_gate.py -q` | 하네스 회귀 검증 | head `d68f45c`에서 107건(110 subtests) PASS. |
| `python scripts/harness_gate.py --issue 88 --branch claude/issue-88-doc-cleanup --base-ref eb40682 --check-links --include-worktree` | Issue evidence 형식·정합성·선언 Markdown 링크 검사 | evidence 파일 3개(commands, manual-qa, metrics)와 verification.md 작성 전에는 FAIL(누락 지적); 전체 작성 뒤 결과는 `verification.md`에 기록. |
| `git diff --check` | 공백 오류 검사 | PASS. |
