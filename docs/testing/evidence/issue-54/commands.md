# Commands

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `python3 -c "...sys.stdout=io.TextIOWrapper(...,encoding='cp949',errors='strict'); print('harness ✗ failed')"` | 수정 전 cp949 콘솔 크래시 재현 | `UnicodeEncodeError: 'cp949' codec can't encode character '✗'`로 크래시. |
| `python3 -c "...harden_console_encoding()... print('harness ✗ failed: verification.md에 Issue #54 기록이 없습니다.')"` (`PYTHONIOENCODING=cp949`) | 수정 후 무크래시 확인 | 크래시 없이 `harness ✗ failed: ...`로 출력하고 `no crash`까지 도달. |
| `python -m pytest scripts/tests/test_harness_gate.py -q` | 하네스 계약 회귀 검증(구현 전·후 각 단계) | head `d21654e`에서 106건(110 subtests) PASS. |
| `grep -rn "gradlew\.bat" --include="*.md" .`(evidence/ 제외) | 정본 문서 Gradle 명령 OS 중립화 완료 확인 | OS별 주석 5줄만 남았고 실행 지시문 형태의 하드코딩은 0건. |
| `python scripts/harness_gate.py --issue 54 --branch claude/issue-54-os-encoding-compat --base-ref a6bd08e --check-links --include-worktree` | Issue evidence 형식·정합성·선언 Markdown 링크·변경 범위 검사 | evidence 파일 3개(commands, manual-qa, metrics)와 verification.md 작성 전에는 FAIL(누락 지적); 전체 작성 뒤 재실행 결과는 `verification.md`에 기록. |
| `git diff --check` | 공백 오류 검사 | PASS. |
