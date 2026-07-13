# Issue Attempt Log

Issue: #15
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/15
Branch: codex/issue-15-dlt-replay

## Attempt 1

### Generate

- 시작 시각: `2026-07-13T17:00:07+09:00`.
- GitHub Issue #15와 Kafka·복구·운영 runbook 정본을 읽고 선택 재발행 계약을 대조했습니다.
- production/test/script 구현은 시작하지 않았습니다.

### Evaluate

- BLOCKED. 승인된 메시지만 재발행한다는 상위 정책은 있지만 script가 안전하게 판단할 선택 식별자, 승인 증적, header 처리, processed_event 경쟁 조건의 계약이 없습니다.
- Docker CLI는 설치되어 있으나 Docker Desktop Linux daemon에 연결할 수 없어 Level 4와 Level 5 실제 검증 환경도 사용할 수 없습니다.

### Failure Cause

- 정책 미결정. 정본은 “운영자가 승인한 메시지”, “processed_event를 확인”, “원본 topic으로 재발행”만 선언합니다. 다음 결정이 없습니다.
  - DLT record를 topic/partition/offset으로 고정 선택할지와 offset 범위·재조회 규칙.
  - 운영자 승인과 원인 분류를 script가 어떤 입력 또는 증적으로 받아야 하는지.
  - `KafkaHeaders.DLT_ORIGINAL_*`와 예외 header를 재발행 전 검증·보존·제거할지.
  - `processed_event` 조회와 정상 consumer 처리 사이의 경쟁을 script 결과로 어떻게 보고할지.
- 환경 불가. `docker version`이 `//./pipe/dockerDesktopLinuxEngine` 부재로 실패했습니다.

### Change Scope

- 이번 Attempt의 허용 변경은 BLOCKED 근거를 Issue #15 evidence에 기록하는 문서 파일뿐입니다.
- 정책 확정 뒤에만 직접 필요한 script·production/test, scripts README, recovery runbook을 변경합니다.

### Reverification

- `git diff --check`를 실행했고 evidence 작성 전 exit code `0`이었습니다.
- `python scripts/harness_gate.py --issue 15 --branch codex/issue-15-dlt-replay --base-ref origin/main --check-links --include-worktree`는 `Issue #15 required Level 5 PASS is missing`으로 exit code `1`이었습니다. Level 5가 필수인데 실제 runtime 검증을 수행하지 못했으므로 의도된 차단 결과입니다.
- script 구현과 Level 4·5 검증은 정책과 Docker daemon이 모두 준비될 때까지 실행하지 않았습니다.

### Next Attempt

- 사람 또는 Spec Agent가 selection, approval, header, processed_event 경쟁 정책을 ADR 또는 Issue acceptance criteria로 확정합니다.
- Docker Desktop Linux daemon을 시작한 뒤 Kafka·Redis·MySQL이 포함된 Level 4와 Level 5 검증을 다시 계획합니다.
