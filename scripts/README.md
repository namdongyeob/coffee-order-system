# 저장소 운영 스크립트

## Git 훅 설치

```powershell
python scripts/install_git_hooks.py
git config --local --get core.hooksPath
```

- `pre-commit`: `main`과 `master` 직접 커밋을 차단합니다.
- `pre-push`: branch 이름의 `issue-N`을 읽어 evidence, verification-log, 변경 Markdown 링크를 검사합니다.
- Git 표준 옵션 `--no-verify`로 훅을 우회할 수 있지만, 긴급 상황에서만 사용하고 우회 이유와 재검증 결과를 PR에 남깁니다.

## 하네스 검사

```powershell
python -m unittest discover -s scripts/tests -p "test_*.py"
python scripts/harness_gate.py --issue 23 --base-ref origin/main --check-links --include-worktree
python scripts/harness_gate.py --links-only --base-ref origin/main
```

검사 항목은 Level 5/6 결정과 이유, Attempt 필수 섹션, Issue 검증 로그, 변경 Markdown의 상대 링크입니다. 실패 항목이 있으면 구체적인 경로와 누락 필드를 출력하고 종료 코드 1을 반환합니다.

## workspace 제한으로 Codex CLI 시작

```powershell
powershell -ExecutionPolicy Bypass -File scripts/start_codex_workspace.ps1
```

이 래퍼는 sandbox, approval, config profile, 작업 디렉터리, 추가 쓰기 디렉터리를 바꾸는 인자를 거부하고 `on-request / workspace-write`를 CLI 옵션으로 요청합니다. 현재 환경에서는 `workspace-write`만 적용되고 approval은 전역 `never`가 유지됐으므로 세션 시작 로그를 확인합니다. 이름은 sandbox 범위만 설명하며 완전한 보안 경계를 뜻하지 않습니다.
