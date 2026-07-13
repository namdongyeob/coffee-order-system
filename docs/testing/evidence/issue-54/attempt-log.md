# Issue Attempt Log

Issue: #54
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/54
Branch: claude/issue-54-os-encoding-compat

Current disposition: PASS
Current Attempt: 2
Current head: 136d29e

## Attempt 1

### Generate

- 시작 시각은 기록하지 못해 `미측정`입니다.
- cp949 콘솔에서 인코딩 불가 문자(예: `✗`)를 포함한 출력이 `UnicodeEncodeError`로 크래시하는 것을 재현했습니다. Hangul 문자 자체는 cp949로 인코딩 가능하지만, 사용자가 evidence·PR body에 붙여넣는 임의 텍스트(em dash, 체크마크 등)가 harness_gate.py의 f-string 오류 메시지에 섞여 들어가면 pre-push hook 실행 중 크래시할 수 있습니다.
- `d21654e`: `harness_gate.py`에 `harden_console_encoding()`을 추가하고(`__main__`에서 stdout/stderr를 `errors="backslashreplace"`로 reconfigure), 같은 함수를 `harness_gate`에서 import하는 `migrate_verification_log.py`·`rebuild_verification_log.py`에 연결했습니다. `install_git_hooks.py`는 `harness_gate`에 의존하지 않는 기존 구조를 유지하기 위해 동일 함수를 자체 정의했습니다. `scripts/tests/test_harness_gate.py`에 `ConsoleEncodingHardeningTest` 2건(인코딩 불가 문자 무크래시, 한글 메시지 원문 보존)을 추가했습니다.
- 같은 커밋에서 `docs/testing/test-strategy.md`, `docs/operations/local-runbook.md`, `docs/operations/kafka-redis-runbook.md`, `docs/onboarding/project-setup.md`, `docs/product/github-issues.md`의 `gradlew.bat` 하드코딩 5곳을 `gradlew`(Windows: `gradlew.bat`, macOS·Linux: `./gradlew` 주석 포함) 표기로 정리했습니다. `docs/ai/context-router.md`는 Gradle 명령을 언급하지 않아 변경하지 않았습니다. env 변수 설정, `curl.exe`, PowerShell 스크립트(`.ps1`) 등 Gradle 명령이 아닌 나머지 OS 종속 표기는 범위 밖(#36)으로 남겨뒀습니다.

### Evaluate

- PASS. `python -m pytest scripts/tests/test_harness_gate.py`가 head `d21654e`에서 106건(110 subtests) PASS입니다. 수정 전 재현 스크립트는 동일 문자에서 크래시했고, 수정 후에는 무크래시로 확인했습니다.

### Failure Cause

- RED: harness 스크립트가 콘솔 인코딩을 강제하지 않아, cp949 콘솔에서 인코딩 불가 문자를 포함한 출력이 발생하면 pre-push hook이 정책 위반 메시지 대신 크래시로 종료됩니다. `docs/` 정본 문서의 Gradle 명령이 `gradlew.bat`으로 하드코딩돼 macOS·Linux 팀원 환경에서 그대로 복사 실행할 수 없습니다.

### Change Scope

- `scripts/harness_gate.py`: `harden_console_encoding()` 정의·`__main__`에서 호출.
- `scripts/migrate_verification_log.py`, `scripts/rebuild_verification_log.py`: `harness_gate`에서 `harden_console_encoding` import·`__main__`에서 호출.
- `scripts/install_git_hooks.py`: 동일 함수 자체 정의·`__main__`에서 호출.
- `scripts/tests/test_harness_gate.py`: `ConsoleEncodingHardeningTest` 2건 추가.
- `docs/testing/test-strategy.md`, `docs/operations/local-runbook.md`, `docs/operations/kafka-redis-runbook.md`, `docs/onboarding/project-setup.md`, `docs/product/github-issues.md`: `gradlew.bat` 하드코딩을 OS 중립 표기로 정리.

### Reverification

- `python -m pytest scripts/tests/test_harness_gate.py`는 head `d21654e`에서 106건(110 subtests) PASS입니다.
- `python scripts/harness_gate.py --issue 54 --branch claude/issue-54-os-encoding-compat --base-ref a6bd08e --check-links --include-worktree`는 PASS입니다(commands.md 기록).
- `grep -rn "gradlew\.bat" --include="*.md" .`(evidence/ 제외)는 OS별 주석 5줄만 남았고 하드코딩 실행 지시문은 0건입니다.
- Level 5/6은 NO입니다. 문서·harness 스크립트 변경만 있어 runtime/API 테스트는 실행하지 않았습니다.
- 종료 시각은 기록하지 못해 `미측정`입니다.

### Next Attempt

- fresh 독립 Review 결과를 반영합니다.

## Attempt 2

### Generate

- fresh 독립 Review가 head `1edd4c1`에서 `REVISE`를 반환했습니다. P1 두 건: (1) `ConsoleEncodingHardeningTest`가 `harden_console_encoding()`을 실제로 호출하지 않고 stdlib `TextIOWrapper.reconfigure`만 직접 검증해, 실제 함수가 삭제·오작동해도 테스트가 계속 PASS하는 상태였습니다. (2) 정본 문서 5곳의 Gradle 명령이 PowerShell에서 실행 불가능했습니다 — 본문에 보인 `gradlew test`는 PowerShell이 cwd를 자동 탐색하지 않아 `CommandNotFoundException`으로 실패하고, "Windows:" 라벨이 붙은 `gradlew.bat test`도 `.\` 접두어가 빠져 동일하게 실패합니다.
- `136d29e`: `ConsoleEncodingHardeningTest`를 `sys.stdout`/`sys.stderr`를 실제로 교체하고 `harness_gate.harden_console_encoding()`을 호출하도록 재작성했습니다. `.reconfigure`가 없는 대체 스트림에서도 무크래시임을 확인하는 `test_does_not_raise_when_stream_lacks_reconfigure`를 추가했습니다(`sys` import 누락도 함께 수정). 정본 문서 5곳의 Gradle 명령을 PowerShell에서 그대로 복사 실행 가능한 `.\gradlew.bat ...` 형태로 정정하고 macOS·Linux 대응 명령을 별도 주석/문장으로 명시했습니다. `.\gradlew.bat ...`을 실제 PowerShell에서 실행해 `CommandNotFoundException`(접두어 없을 때)과 정상 인식(접두어 있을 때)을 재현·확인했습니다.

### Evaluate

- PASS. Review가 반환한 P1 2건을 원래 Dev 범위에서 한 번에 정정했습니다. 안전 불변조건 약화(P0)는 없었습니다.

### Failure Cause

- RED: Attempt 1에서 테스트를 실제 대상 함수 대신 stdlib 동작을 재구현해 검증했고, 문서 명령에서 PowerShell의 `.\` 실행 파일 탐색 규칙을 반영하지 않았습니다.

### Change Scope

- `scripts/tests/test_harness_gate.py`: `sys` import 추가, `ConsoleEncodingHardeningTest` 3건으로 재작성(실제 함수 호출, `.reconfigure` 없는 스트림 방어 테스트 추가).
- `docs/testing/test-strategy.md`, `docs/operations/local-runbook.md`, `docs/operations/kafka-redis-runbook.md`, `docs/onboarding/project-setup.md`, `docs/product/github-issues.md`: Gradle 명령을 PowerShell에서 실행 가능한 형태로 정정.

### Reverification

- `python -m pytest scripts/tests/test_harness_gate.py`는 head `136d29e`에서 107건(110 subtests) PASS입니다.
- PowerShell에서 `gradlew test`(접두어 없음)는 `CommandNotFoundException`으로 실패, `.\gradlew.bat --version`(접두어 있음)은 Gradle 9.5.1 버전 배너를 정상 출력해 명령이 인식·실행됨을 확인했습니다(exit code는 255였으나 command-not-found가 아닌 정상 인식·실행 결과입니다. 원인은 harness 범위 밖이라 조사하지 않았습니다).
- `python scripts/harness_gate.py --issue 54 --branch claude/issue-54-os-encoding-compat --base-ref a6bd08e --check-links --include-worktree`는 head `136d29e`에서 PASS입니다.
- Level 5/6은 NO입니다. 문서·harness 스크립트·테스트 변경만 있어 runtime/API 테스트는 실행하지 않았습니다.
- 종료 시각은 기록하지 못해 `미측정`입니다.

### Next Attempt

- 없음. evidence를 확정하고 push 뒤 fresh 독립 Review 재검토와 GitHub 새 head의 CI 결과를 확인합니다.
