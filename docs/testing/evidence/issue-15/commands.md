# Issue #15 Commands

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `git fetch origin; git rebase origin/main` | 최신 `origin/main` 기준으로 Issue #15 branch 정렬 | PASS. `287594549cbd6b66eb85a3dd74285125ed36906b`로 충돌 없이 리베이스했습니다. |
| `./gradlew.bat test --tests "*DltReplayServiceIntegrationTest" --no-daemon --max-workers=1` | original offset 단독 누락 fail-closed와 기존 DLT replay 계약의 Level 4 통합 검증 | PASS. Testcontainers Kafka·Redis·MySQL에서 tests 4, failures 0, errors 0이었습니다. |
| `./scripts/replay_dlt_message.ps1 -Partition 0 -Offset 0 -ApprovedBy operator-a -Reason 'Offset header validation'` | local Compose runtime에서 DLT 단건 선택의 Level 5 fail-closed 확인 | PASS. MySQL·Redis·Kafka 연결 뒤 존재하지 않는 DLT offset을 10초 제한으로 차단했고 `DltReplayException` 및 exit code `1`로 재발행 없이 종료했습니다. |
| `git diff --check origin/main...HEAD` | execution head의 공백 오류 확인 | PASS. exit code `0`이었습니다. |
| `python scripts/harness_gate.py --issue 15 --branch codex/issue-15-dlt-replay --base-ref origin/main --check-links --include-worktree` | Issue evidence와 execution head 정합성 확인 | PASS. exit code `0`이었습니다. |
| `python scripts/harness_gate.py --issue 15 --branch codex/issue-15-dlt-replay --base-ref origin/main --check-links --include-worktree --pr-body-file C:\Users\user\AppData\Local\Temp\issue-15-pr-body.md` | PR body와 Issue evidence의 execution mode·Attempt·head·Level PASS preflight 확인 | PASS. exit code `0`이었습니다. |

실행하지 않은 명령은 결과로 기록하지 않습니다. full Gradle 회귀는 이번 current diff가 DLT 통합 테스트와 Issue evidence만 변경하므로 Dev focused 검증 범위를 넘겨 실행하지 않았고, STRICT의 최종 전체 회귀는 GitHub Actions `quality-gates` 소유입니다.
