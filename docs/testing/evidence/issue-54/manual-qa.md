# Manual QA

- 이 Issue는 harness 스크립트(`scripts/*.py`)의 콘솔 인코딩 방어와 정본 문서의 Gradle 명령 표기만 변경합니다. production 애플리케이션 코드, API, DB, Kafka, Redis는 변경하지 않습니다.
- Level 5/6은 Issue 본문 결정대로 NO이며 runtime 또는 HTTP 검증을 완료로 표현하지 않습니다.
- cp949 콘솔 크래시를 직접 텍스트 스트림으로 재현·수정 확인했습니다(`commands.md`). 실제 Windows cp949 코드페이지 콘솔(`chcp 949`)에서의 대화형 실행은 이번 세션 환경(Git Bash 도구)이 실제 Windows 콘솔이 아니라서 직접 관찰하지 못했고, 대신 Python `io.TextIOWrapper(encoding="cp949")`로 동일한 인코딩 계약을 재현해 `UnicodeEncodeError` 발생·해소를 코드 수준에서 확인했습니다. 이 제약은 `attempt-log.md`에 남깁니다.
- `docs/product/github-issues.md`는 실제 GitHub Issue 초안 템플릿(향후 복사해 사용)이라 canonical 정본으로 판단해 포함했고, `docs/testing/evidence/issue-*/` 하위 과거 evidence 기록은 특정 시점 실행 로그이므로 소급 수정하지 않았습니다.
- `scripts/README.md`의 `replay_dlt_message.ps1` 설명은 실제 PowerShell 전용 스크립트의 동작 설명이라 OS 중립화 대상이 아니라고 판단해 변경하지 않았습니다.
- 안전 불변조건(단일 작성자, 읽기 전용 Review/QA, 최신 head CI PASS, 하네스 변경 동결 예외 근거)은 유지했습니다. 이 변경은 실측 cp949 크래시 재현과 gradlew.bat 하드코딩 실측이라는 관찰 가능한 근거(오케스트레이션 정책의 하네스 변경 동결 예외 조건)에 해당합니다.
- 문서·harness 스크립트 변경 Issue이므로 Gradle 애플리케이션 빌드, runtime, API 테스트는 의도적으로 실행하지 않았습니다.
