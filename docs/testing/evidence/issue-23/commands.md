# Issue #23 Commands

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `python -m unittest discover -s scripts/tests -p "test_*.py"` | 하네스 검사 RED 확인 | FAIL, 10건 실패/오류. 구현 전 기준 실패 확인. |
| `python -m unittest discover -s scripts/tests -p "test_*.py"` | 하네스 검사 GREEN 확인 | PASS, 10건. |
| `python -m unittest discover -s scripts/tests -p "test_*.py"` | Review 지적 반영 후 강화된 하네스 검사 | PASS, 16건. |
| Codex ephemeral 세션 시작 | 프로젝트 `.codex/config.toml` 적용 확인 | FAIL, 전역 `never / danger-full-access`가 유지됨. |
| `scripts/start_codex_workspace.ps1` ephemeral 세션 | 제한 CLI 옵션 확인 | PARTIAL, `workspace-write` 적용. approval은 `never` 유지. |
| `python scripts/harness_gate.py --issue 23 --base-ref origin/main --check-links` | evidence와 변경 문서 링크 검사 | PASS. |
| `python scripts/harness_gate.py --issue 23 --base-ref origin/main --check-links --include-worktree` | 미커밋 문서를 포함한 최종 repository gate | PASS. |
| `python scripts/harness_gate.py --check-branch --branch main` | 보호 branch 차단 확인 | 의도된 FAIL, 종료 코드 1. |
| `python scripts/harness_gate.py --check-branch --branch codex/issue-23-harness-quality-gates` | Issue branch 허용 확인 | PASS. |
| `python scripts/install_git_hooks.py` | versioned hook 설치 | PASS, `core.hooksPath=.githooks`. |
| `git hook run pre-commit`, `git hook run pre-push` | 실제 hook 실행 | PASS. |
| `.\gradlew.bat compileJava --no-daemon` | Java compile regression | PASS, 22초. |
| `.\gradlew.bat test --no-daemon` | 전체 regression 첫 실행 | FAIL, Docker daemon 미가동으로 Testcontainers client 생성 실패. |
| `docker info --format '{{.ServerVersion}} {{.OSType}}'` | Docker root cause 확인 후 daemon 준비 | PASS, `29.4.2 linux`. |
| `.\gradlew.bat test --no-daemon` | 동일 전체 regression 재실행 | PASS, 1분 48초. |
| `scripts/start_codex_workspace.ps1 --sandbox=danger-full-access` | 래퍼 권한 인자 덮어쓰기 차단 | 의도된 거부, 종료 코드 2. |
| `scripts/start_codex_workspace.ps1 -C C:\` | 래퍼 작업 루트 덮어쓰기 차단 | 의도된 거부, 종료 코드 2. |
| Python `yaml.safe_load` | GitHub Actions workflow 문법 확인 | PASS. |
| `.\gradlew.bat test --no-daemon` | Review 수정 후 최종 전체 regression | PASS, 31초. |

독립 Review 결과와 반영 여부는 PR 본문에 추가합니다.
