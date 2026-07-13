# Issue #15 Commands

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `git status -sb; git branch --show-current; git rev-parse HEAD` | worktree와 기준 head 확인 | PASS. `codex/issue-15-dlt-replay`, `d1326bdc81d4b2b62c9b11eb0083e7da99ea1de8`, 시작 시 미커밋 변경 없음. |
| `gh issue view 15 --repo namdongyeob/coffee-order-system --json number,title,body,url,labels,comments` | GitHub Issue 정본 확인 | PASS. STRICT, Level 4 YES, Level 5 YES, Level 6 NO와 DLT 조회·선택 재발행·processed_event 확인 범위를 확인했습니다. |
| `Get-Content -Raw -Encoding UTF8 docs/architecture/recovery-strategy.md` | DLT 복구 계약 확인 | PASS. 승인된 메시지만 원본 topic으로 재발행하는 script라는 방향만 있고 선택·승인·header·경쟁 정책은 없습니다. |
| `Get-Content -Raw -Encoding UTF8 docs/operations/kafka-redis-runbook.md` | 운영 DLT runbook 확인 | PASS. Kafka UI/CLI 조회, 원인 표, processed_event 확인은 있으나 script 계약은 없습니다. |
| `docker version` | Level 4·5 Docker runtime 가용성 확인 | PARTIAL. Docker CLI `29.4.2`는 확인됐으나 Docker Desktop Linux daemon npipe가 없어 연결 실패했습니다. |
| `docker compose version` | Compose CLI 확인 | PASS. `v5.1.3`. daemon 연결 실패와 별개입니다. |
| `git diff --check` | 현재 diff 공백 오류 확인 | PASS. evidence 작성 전 exit code `0`. |
| `python scripts/harness_gate.py --issue 15 --branch codex/issue-15-dlt-replay --base-ref origin/main --check-links --include-worktree` | Issue evidence와 repository 계약 확인 | FAIL. `Issue #15 required Level 5 PASS is missing`. 실제 Level 5가 필수지만 Docker daemon 부재로 실행하지 못한 현재 BLOCKED 상태를 정확히 반영합니다. |

실행하지 않은 명령은 결과로 기록하지 않습니다. 정책 미결정과 Docker daemon 부재 때문에 TDD RED/GREEN, Gradle focused/full regression, Kafka·Redis·MySQL Level 4, local runtime Level 5는 시작하지 않았습니다.
