# 실행 명령과 결과

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `python scripts/harness_gate.py --issue 28 --base-ref origin/main --check-links --include-worktree` | Issue evidence와 변경 Markdown 링크 검사 | PASS. Issue evidence와 변경 Markdown 상대 링크를 확인했습니다. |
| `git diff --check` | 공백·패치 형식 정적 검사 | PASS. 오류 출력이 없었습니다. CRLF 변환 경고는 Git 작업 트리 경고이며 diff 오류가 아닙니다. |
| `.\gradlew.bat test --no-daemon` | CI와 동일한 전체 Gradle 회귀 확인 | PASS. `compileJava`, `compileTestJava`, `testClasses`가 완료되었고 Gradle 종료 코드 0을 확인했습니다. |
| `python -m unittest scripts.tests.test_harness_gate` | 문서 하네스 단위 검증 | PASS. 48개 테스트가 통과했습니다. |
| ADR core section coverage PowerShell 검사 | ADR 7개 필수 섹션과 상세 ADR 표 열 확인 | PASS. 7개 ADR의 core section과 ADR-002/003/005의 상세 열을 확인했습니다. |
| 변경 경로 scope PowerShell 검사 | 허용 파일 범위 확인 | PASS. 변경 파일이 `docs/adr/*`, Issue #28 evidence, verification log 범위에만 있음을 확인했습니다. |

이 결과는 Dev의 실행 기록입니다. 독립 Combined Verifier와 GitHub Actions CI 결과는 아직 PASS 근거가 아닙니다.
