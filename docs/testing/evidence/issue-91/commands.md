# Commands

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `gh issue view 91 --json title,body,comments,state` | Issue #91 본문 확인 | 코멘트 없음. 1차 범위(Dev 병렬만, 3-Dev 예외 제외, gitignored state, 4종 메시지, 자동 merge 금지)와 완료 기준을 그대로 확인. |
| 사용자에게 범위 확인 질문(AskUserQuestion) | 1차 범위 전체를 한 PR로 진행할지, 설계 문서 먼저 분리할지 확인 | "1차 범위 그대로 한 PR" 선택 확정. |
| `python -c "import ast; ast.parse(...)"` | `scripts/team_orchestration.py` 문법 검증 | 오류 없음. |
| `python scripts/team_orchestration.py status` | 빈 상태에서 기본 동작 확인 | 빈 assignments, 9개 metrics 키 반환. |
| `python scripts/team_orchestration.py register/release/message` 수동 실행(menu/point 비중첩, order 3번째 writer, 겹치는 menu/service 경로) | CLI 수동 smoke: 2개 등록 성공, 3번째 writer BLOCKED, 겹치는 경로 SCOPE_CONFLICT, reader는 writer 슬롯 미소비 | 설계대로 전부 동작 확인(BLOCKED/SCOPE_CONFLICT 메시지의 "1차" 한글 표기는 Git Bash 콘솔 렌더링 한계로 깨져 보이나 크래시 없음 — `harden_console_encoding()` 적용 후 재확인). |
| `python -m pytest scripts/tests/test_team_orchestration.py -q` | 신규 테스트 전체 실행 | 30 passed, 6 subtests passed. |
| `python -m pytest scripts/tests/ -q` | 전체 scripts 테스트(harness_gate + team_orchestration) 회귀 확인 | 160 passed, 121 subtests passed(기존 130+신규 30, 충돌 없음). |
| `git status --short` | `.team-orchestration-state/`가 git에 노출되지 않는지 확인 | gitignore 반영 뒤 상태 디렉터리가 `git status`에 나타나지 않음. `.gitignore`, `scripts/team_orchestration.py`, `scripts/tests/test_team_orchestration.py`만 변경분으로 표시. |
| `python scripts/harness_gate.py --issue 91 --branch claude/issue-91-parallel-orchestration-tool --base-ref e8ea5ed --check-links --include-worktree` | PR 전 preflight | 결과는 `verification.md`에 기록. |
