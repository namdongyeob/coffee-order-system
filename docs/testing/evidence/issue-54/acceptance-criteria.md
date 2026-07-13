# Issue #54 Acceptance Criteria

Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/54

Execution mode: STRICT
Execution mode reason: harness 스크립트(workflow policy)를 변경합니다.
Level 5 required: NO
Level 5 reason: 애플리케이션 런타임 변경이 없습니다.
Level 6 required: NO
Level 6 reason: HTTP 계약 변경이 없습니다.

## 완료 기준

- [x] 고정된 실행 환경(Windows cp949 콘솔, CI Linux)에서 지정된 한글 오류 메시지가 기대 문자열로 정확히 표시됨(재현 명령 포함). `harden_console_encoding()`을 4개 스크립트(`harness_gate.py`, `migrate_verification_log.py`, `rebuild_verification_log.py`, `install_git_hooks.py`)의 `__main__` 진입점에 추가해 cp949 콘솔에서 `UnicodeEncodeError`로 죽지 않고, 표현 가능한 한글 오류 메시지는 원문 그대로 유지됩니다. `commands.md`에 크래시 재현·수정 후 무크래시·한글 원문 보존 3개 명령과 결과를 기록했습니다.
- [x] hot path·정본 문서의 명령 표기가 OS 중립이며 변경 목록이 기록됨. `docs/testing/test-strategy.md`, `docs/operations/local-runbook.md`, `docs/operations/kafka-redis-runbook.md`, `docs/onboarding/project-setup.md`, `docs/product/github-issues.md` 5개 파일의 `gradlew.bat` 하드코딩을 `gradlew`(Windows/macOS·Linux 주석 포함) 표기로 정리했습니다. 변경 목록은 `attempt-log.md`의 Change Scope에 있습니다. `docs/ai/context-router.md`(Router hot path 본문)에는 Gradle 명령 언급이 없어 변경 대상이 없었습니다. 전체 `docs/` 일괄 교체는 하지 않았고 나머지 문서 감사는 #36으로 이월합니다.
- [x] 하네스 suite 회귀 없음. `python -m pytest scripts/tests/test_harness_gate.py`가 head `d21654e`에서 106건(110 subtests) PASS입니다.
- [x] 기본 evidence 파일과 `verification.md`를 작성했습니다.

검증 실행 head는 `d21654e`입니다.
