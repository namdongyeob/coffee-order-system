# Commands

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `find docs -type f -name "*.md" ! -path "docs/testing/evidence/*"` | non-evidence 문서 인벤토리 수집 | 54개 파일. |
| `grep -rl -e <basename> -e <path> docs README.md AGENTS.md CLAUDE.md .codex .github` (파일별 반복) | 각 문서의 실제 참조 경로 확인 | 결과를 `docs/ai/doc-lifecycle.md`의 근거 열에 반영. |
| `grep -n "docs/" README.md` | 사람 온보딩 색인(README) 링크 목록 확인 | 11개 `docs/` 링크 확인. `erd.md`·`lecture-mapping.md`가 여기 포함돼 AI hot path 미참조 오탐임을 확인. |
| `grep -n "ADR-001\|ADR-006" docs/adr/README.md` | ADR README가 개별 ADR을 색인하는지 확인 | 매치 없음 — ADR README는 운영 규칙 문서일 뿐 개별 ADR을 링크하지 않음. |
| `python -m pytest scripts/tests/test_harness_gate.py -q` | 하네스 회귀 검증 | head `7ee1180`에서 107건(110 subtests) PASS. |
| `python scripts/harness_gate.py --issue 36 --branch claude/issue-36-doc-lifecycle-audit --base-ref 7d32e2d --check-links --include-worktree` | Issue evidence 형식·정합성·선언 Markdown 링크 검사 | evidence 파일 3개(commands, manual-qa, metrics)와 verification.md 작성 전에는 FAIL(누락 지적); 전체 작성 뒤 결과는 `verification.md`에 기록. |
| `git diff --check` | 공백 오류 검사 | PASS. |
