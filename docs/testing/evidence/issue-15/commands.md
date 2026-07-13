# Issue #15 Commands

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `git fetch origin; git rebase origin/main` | 최신 `origin/main` 기준으로 Issue #15 branch 정렬 | PASS. `287594549cbd6b66eb85a3dd74285125ed36906b`로 충돌 없이 리베이스했습니다. |
| `./gradlew.bat test --tests "*DltReplayServiceIntegrationTest" --no-daemon --max-workers=1` | original offset 단독 누락 fail-closed와 기존 DLT replay 계약의 Level 4 통합 검증 | PASS. Testcontainers Kafka·Redis·MySQL에서 tests 4, failures 0, errors 0이었습니다. |
| `./scripts/replay_dlt_message.ps1 -Partition 0 -Offset 0 -ApprovedBy operator-a -Reason 'Offset header validation'` | local Compose runtime에서 DLT 단건 선택의 Level 5 fail-closed 확인 | PASS. MySQL·Redis·Kafka 연결 뒤 존재하지 않는 DLT offset을 10초 제한으로 차단했고 `DltReplayException` 및 exit code `1`로 재발행 없이 종료했습니다. |
| `./gradlew.bat test --no-daemon --max-workers=1` | execution head 이후 전체 Gradle 회귀 시도 | PARTIAL. Testcontainers MySQL context 재시도가 3분을 넘겨도 완료되지 않아 명령을 중단했습니다. 종료 코드, 테스트 수, failures/errors, `BUILD SUCCESSFUL` 또는 `BUILD FAILED`는 관찰하지 못했습니다. |
| `git diff --check origin/main...HEAD` | execution head의 공백 오류 확인 | PASS. exit code `0`이었습니다. |
| `python scripts/harness_gate.py --issue 15 --branch codex/issue-15-dlt-replay --base-ref origin/main --check-links --include-worktree` | Issue evidence와 execution head 정합성 확인 | PASS. exit code `0`이었습니다. |
| `python scripts/harness_gate.py --issue 15 --branch codex/issue-15-dlt-replay --base-ref origin/main --check-links --include-worktree --pr-body-file C:\Users\user\AppData\Local\Temp\issue-15-pr-body.md` | PR body와 Issue evidence의 execution mode·Attempt·head·Level PASS preflight 확인 | PASS. exit code `0`이었습니다. |

실행하지 않은 명령은 결과로 기록하지 않습니다. full Gradle 회귀는 execution head 이후 실제로 시작했지만 Testcontainers MySQL context 재시도 정체로 완료 전 중단했습니다. 이 PARTIAL 관찰은 Level 4·5 PASS 또는 GitHub Actions `quality-gates`의 최종 전체 회귀 판정을 대체하지 않습니다.
