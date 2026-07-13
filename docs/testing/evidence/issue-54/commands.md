# Commands

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `python3 -c "...sys.stdout=io.TextIOWrapper(...,encoding='cp949',errors='strict'); print('harness ✗ failed')"` | 수정 전 cp949 콘솔 크래시 재현 | `UnicodeEncodeError: 'cp949' codec can't encode character '✗'`로 크래시. |
| `python3 -c "...harden_console_encoding()... print('harness ✗ failed: verification.md에 Issue #54 기록이 없습니다.')"` (`PYTHONIOENCODING=cp949`) | 수정 후 무크래시 확인 | 크래시 없이 `harness ✗ failed: ...`로 출력하고 `no crash`까지 도달. |
| `python -m pytest scripts/tests/test_harness_gate.py -q` | 하네스 계약 회귀 검증(구현 전·후 각 단계) | head `d21654e`에서 106건(110 subtests) PASS; fresh Review REVISE 반영 뒤 head `136d29e`에서 107건(110 subtests) PASS. |
| `grep -rn "gradlew\.bat" --include="*.md" .`(evidence/ 제외) | 정본 문서 Gradle 명령 OS 중립화 완료 확인 | OS별 주석·`.\` 접두어 포함 실행 가능 명령만 남았고 실행 지시문 형태의 하드코딩은 0건. |
| `python scripts/harness_gate.py --issue 54 --branch claude/issue-54-os-encoding-compat --base-ref a6bd08e --check-links --include-worktree` | Issue evidence 형식·정합성·선언 Markdown 링크·변경 범위 검사 | 초기(evidence 3개 누락 시)에는 FAIL로 누락 지적; 전체 작성 뒤 head `d21654e`·`136d29e`에서 PASS. |
| `git diff --check` | 공백 오류 검사 | PASS. |
| PowerShell `gradlew test` (접두어 없음) | 문서 명령의 실제 실행 가능성 확인(REVISE 대응) | `CommandNotFoundException`으로 실패. |
| PowerShell `.\gradlew.bat --version` (접두어 있음) | 정정한 문서 명령의 실제 실행 가능성 확인 | Gradle 9.5.1 버전 배너 정상 출력(명령 인식·실행됨). exit code 255는 관찰했으나 원인은 이 Issue 범위 밖이라 조사하지 않았습니다. |
| fresh 독립 Review at `1edd4c1` | Dev와 분리된 fresh context의 1차 독립 검토 | `REVISE`. P1 2건(테스트가 실제 함수를 호출하지 않음, 정본 문서 Gradle 명령이 PowerShell에서 실행 불가) 반환. 안전 불변조건 약화(P0)는 없음. |
| 독립 QA at `1edd4c1` | Dev·Review와 분리된 독립 검증 | `PASS`. pytest 106 PASS, cp949 크래시·수정·한글 원문 보존 재현, gradlew.bat 하드코딩 잔존 0건, harness gate PASS, Level 5/6 NO 근거(runtime/API 파일 미변경)를 독립 확인. |
