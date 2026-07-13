# Issue Attempt Log

Issue: #15
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/15
Branch: codex/issue-15-dlt-replay
Current disposition: PASS
Current Attempt: 2
Current head: a49e0103d938f8f078601afb4502e04a5f7ded73

## Attempt 1

### Generate

- 정책·Docker daemon 부재를 확인하고 구현을 안전 정지했습니다.

### Evaluate

- BLOCKED. 당시 Issue safety contract와 Docker runtime을 사용할 수 없었습니다.

### Failure Cause

- 정책 미결정과 Docker Desktop Linux daemon 미연결이었습니다.

### Change Scope

- Issue #15 evidence만 기록했습니다.

### Reverification

- `python scripts/harness_gate.py --issue 15 --branch codex/issue-15-dlt-replay --base-ref origin/main --check-links --include-worktree`는 필수 Level 5 PASS 부재로 exit code `1`이었습니다.

### Next Attempt

- Issue safety contract 승인과 Docker runtime 가용성을 확인한 뒤 구현·검증을 재개합니다.

## Attempt 2

### Generate

- 시작 시각: `2026-07-13T18:30:46+09:00`.
- `origin/main`의 `287594549cbd6b66eb85a3dd74285125ed36906b`로 리베이스했고 충돌은 없었습니다.
- original partition은 존재하지만 original offset만 없는 DLT record가 fail-closed되는 통합 테스트를 추가했습니다.

### Evaluate

- PASS. execution head `a49e0103d938f8f078601afb4502e04a5f7ded73`에서 focused Level 4 통합 테스트 4건은 failures 0, errors 0이었습니다.
- local Compose MySQL·Redis·Kafka 환경에서 Level 5 script는 존재하지 않는 DLT offset을 10초 제한 뒤 exit code `1`로 차단했고 재발행하지 않았습니다.

### Failure Cause

- 없음. offset 검증 구현은 이미 `requireHeader`에 있었고, 기존 테스트가 partition·offset을 함께 생략해 offset 단독 실패 경로를 증명하지 못한 것이 누락 원인이었습니다.

### Change Scope

- `src/test/java/com/example/coffeeordersystem/recovery/DltReplayServiceIntegrationTest.java`의 original offset 누락 회귀 테스트와 Issue #15 evidence·PR metadata 동기화만 허용했습니다.
- production 코드, 공개 API, 자동 재발행, 범위 밖 리팩터링은 변경하지 않았습니다.

### Reverification

- `./gradlew.bat test --tests "*DltReplayServiceIntegrationTest" --no-daemon --max-workers=1`는 Testcontainers Kafka·Redis·MySQL에서 tests 4, failures 0, errors 0으로 PASS했습니다.
- `./scripts/replay_dlt_message.ps1 -Partition 0 -Offset 0 -ApprovedBy operator-a -Reason 'Offset header validation'`는 local profile과 Compose 인프라 연결 뒤 지정 레코드 부재를 `DltReplayException`으로 종료했습니다. exit code `1`은 재발행 없는 fail-closed 결과이므로 Level 5 PASS입니다.
- 종료 시각: `2026-07-13T18:33:58+09:00`.

### Next Attempt

- 없음.
