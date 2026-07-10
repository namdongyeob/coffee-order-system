# Codex 프로젝트 권한

저장소의 `.codex/config.toml`은 이 프로젝트에서 Codex가 파일과 명령을 다루는 기본 권한을 제한합니다.

```toml
approval_policy = "on-request"
sandbox_mode = "workspace-write"
```

- `workspace-write`는 프로젝트 밖의 임의 쓰기를 기본 차단합니다.
- `on-request`는 더 넓은 권한이 필요한 명령을 사람이 판단하게 합니다.
- 저장소 파일은 이 프로젝트가 원하는 목표 정책을 기록합니다. 현재 Codex CLI가 이를 실제로 로드하는지는 새 세션 시작 로그에서 반드시 확인합니다.
- 데스크톱 CLI `0.144.0-alpha.4` 검증에서는 저장소 설정이 전역 `never / danger-full-access`를 덮어쓰지 못했습니다. 따라서 저장소 파일만으로 권한이 강제됐다고 판단하지 않습니다.
- Codex CLI는 `powershell -ExecutionPolicy Bypass -File scripts/start_codex_workspace.ps1`로 시작해 workspace 제한 옵션을 요청합니다. 래퍼는 sandbox, approval, config profile, 작업 디렉터리, 추가 쓰기 디렉터리를 덮어쓰는 인자를 거부합니다. 현재 검증에서는 `workspace-write`는 적용됐지만 `approval`은 전역 `never`가 유지됐습니다.
- 따라서 현재 클라이언트에서 실제 강제되는 프로젝트 보호는 sandbox와 Git 훅이며, approval 정책은 세션 시작 로그를 보고 별도 전역 설정 또는 제품 권한으로 관리해야 합니다.
- 데스크톱 앱에서는 작업 시작 로그 또는 권한 UI를 확인하고, 적용되지 않으면 전역 설정 변경을 별도로 승인받습니다.

Git 훅은 `python scripts/install_git_hooks.py`로 설치합니다. Git 표준 옵션 `git commit/push --no-verify`는 긴급 우회이며, 사용 이유와 이후 재검증 결과를 PR에 기록해야 합니다.
