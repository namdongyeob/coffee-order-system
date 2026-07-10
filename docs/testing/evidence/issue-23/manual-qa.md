# Issue #23 Manual QA

## 확인 대상

- 새 Codex 세션에서 프로젝트 `.codex/config.toml` 적용 여부.
- `main` branch 차단과 Issue branch 허용.
- evidence 누락 시 harness gate 실패 메시지.
- Level 5/6을 NO로 결정한 이유가 실제 변경 범위와 일치하는지.

## 현재 상태

- 스크립트 단위 테스트는 완료했습니다.
- 새 Codex 세션 권한 확인 결과 저장소 `.codex/config.toml`만으로는 전역 권한을 덮어쓰지 못했습니다.
- 제한 권한 CLI 래퍼는 `workspace-write`를 적용했지만 approval은 `never`가 유지됐습니다.
- Git 훅을 설치하고 Issue branch에서 pre-commit과 pre-push가 통과하는 것을 확인했습니다.
- `main` 입력은 branch guard가 의도대로 차단했습니다.
- Codex wrapper에 sandbox 덮어쓰기 인자를 전달하면 종료 코드 2로 차단되는 것을 확인했습니다.
- Docker daemon 미가동 상태와 가동 상태에서 동일 전체 테스트를 실행해 환경 실패와 코드 회귀를 구분했습니다.
