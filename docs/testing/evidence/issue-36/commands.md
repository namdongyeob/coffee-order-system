# Commands

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `find docs -type f -name "*.md" ! -path "docs/testing/evidence/*"` | non-evidence 문서 인벤토리 수집 | 54개 파일. |
| `grep -rl -e <basename> -e <path> docs README.md AGENTS.md CLAUDE.md .codex .github` (파일별 반복) | 각 문서의 실제 참조 경로 확인 | 결과를 `docs/ai/doc-lifecycle.md`의 근거 열에 반영. |
| `grep -n "docs/" README.md` | 사람 온보딩 색인(README) 링크 목록 확인 | 11개 `docs/` 링크 확인. `erd.md`·`lecture-mapping.md`가 여기 포함돼 AI hot path 미참조 오탐임을 확인. |
| `grep -n "ADR-001\|ADR-006" docs/adr/README.md` | ADR README가 개별 ADR을 색인하는지 확인 | 매치 없음 — ADR README는 운영 규칙 문서일 뿐 개별 ADR을 링크하지 않음. |
| `python -m pytest scripts/tests/test_harness_gate.py -q` | 하네스 회귀 검증 | head `7ee1180`에서 107건(110 subtests) PASS; REVISE 반영 뒤 head `f8756fb`에서도 107건(110 subtests) PASS. |
| `python scripts/harness_gate.py --issue 36 --branch claude/issue-36-doc-lifecycle-audit --base-ref 7d32e2d --check-links --include-worktree` | Issue evidence 형식·정합성·선언 Markdown 링크 검사 | evidence 파일 3개(commands, manual-qa, metrics)와 verification.md 작성 전에는 FAIL(누락 지적); 전체 작성 뒤 head `7ee1180`·`f8756fb`에서 PASS. |
| `git diff --check` | 공백 오류 검사 | PASS. |
| fresh 독립 Combined Verifier at `1fe372e` | Dev와 분리된 fresh context의 독립 검토·검증 | `REVISE`. `docs/adr/README.md`가 54개 인벤토리 분류에서 누락됐다는 지적 1건 반환. 12개 이상의 다른 분류 근거는 직접 재검증해 모두 정확하다고 확인. |
| Verifier 반환 지적 정정 후 `python -m pytest scripts/tests/test_harness_gate.py -q` | REVISE 정정(head `f8756fb`) 뒤 재검증 | 107건 PASS. |
| 인벤토리 완전성 재확인 스크립트(54개 원본 파일이 모두 doc-lifecycle.md에 언급되는지 대조) | 누락 재발 방지 확인 | 누락 0건. |
