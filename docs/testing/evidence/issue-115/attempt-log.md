# Issue Attempt Log

Issue: #115
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/115
Branch: codex/issue-115-troubleshooting-sync
Current disposition: PASS
Current Attempt: 3
Current head: 9b0776208dcbc2aeb1e0fe9bc860699f51d031e2

## Attempt 1

### Generate

- README, ERD, API 명세, 요구사항·범위를 final main 구현과 Flyway V1~V7에 맞췄습니다.
- 실제 TIL 5개와 ADR·검증·제출 링크를 연결했습니다.

### Evaluate

- links-only gate, 문서 하네스 130건, 외부 TIL HTTP, diff scope를 확인해 PASS했습니다.

### Failure Cause

- 없음.

### Change Scope

- `README.md`, `docs/db/erd.md`, `docs/api/api-spec.md`, `docs/product/requirements.md`, `docs/product/scope.md`, Issue #115 evidence만 변경했습니다.

### Reverification

- `python scripts/harness_gate.py --links-only --base-ref origin/main --include-worktree`: PASS.
- `python -m unittest scripts.tests.test_harness_gate`: PASS, 130 tests.
- TIL URL 5개 HEAD: 모두 HTTP 200.
- `git diff --check`: PASS.

### Next Attempt

Review P1 두 건을 원래 Dev가 한 번 수정합니다.

## Attempt 2

### Generate

- ERD에 Flyway V6의 `chk_ranking_rebuild_run_event_type` 제약을 명시했습니다.
- Redis 복구 설명을 Kafka replay temp ZSET 재구성 후 DB 주문 집계 검증으로 정정했습니다.

### Evaluate

- links-only gate, 문서 하네스 130건, diff check가 PASS했습니다.

### Failure Cause

- ERD 제약 목록에서 rebuild event type check가 누락됐습니다.
- README와 API의 요약 표현이 Redis 복구 원천을 DB로 오해할 수 있게 작성됐습니다.

### Change Scope

- `README.md`, `docs/api/api-spec.md`, `docs/db/erd.md`, Issue #115 evidence만 수정했습니다.

### Reverification

- `python scripts/harness_gate.py --links-only --base-ref origin/main --include-worktree`: PASS.
- `python -m unittest scripts.tests.test_harness_gate`: PASS, 130 tests.
- `git diff --check`: PASS.

### Next Attempt

없음. fresh Combined Verifier와 최신 PR-head CI를 확인합니다.

## Attempt 3

### Generate

- 사용자의 산출물 유형 정정에 따라 README의 TIL 5개를 단일 통합 트러블슈팅 페이지로 교체했습니다.

### Evaluate

- 단일 Notion URL HTTP 200, links-only gate와 diff scope를 확인해 PASS했습니다.
- 최초 PR-head CI는 Attempt 3 head가 base SHA를 가리켜 README 변경이 reconciliation 범위에서 누락된 것을 P1으로 탐지했습니다.

### Failure Cause

- 기존 산출물을 TIL로 분류했으나 사용자가 의도한 산출물은 프로젝트 트러블슈팅이었습니다.
- 변경 전 evidence metadata가 실제 README content commit이 아니라 작업 시작 base를 기록했습니다.

### Change Scope

- `README.md`와 Issue #115 evidence만 수정했습니다.

### Reverification

- `Current head`, `Execution head`, verification `Head`를 실제 content commit `9b0776208dcbc2aeb1e0fe9bc860699f51d031e2`로 일치시켰습니다.
- 단일 통합 트러블슈팅 URL HEAD: HTTP 200.
- `python scripts/harness_gate.py --links-only --base-ref origin/main --include-worktree`: PASS.
- `python -m unittest scripts.tests.test_harness_gate`: PASS, 134 tests.
- `git diff --check`: PASS.

### Next Attempt

없음. fresh Combined Verifier와 최신 PR-head CI를 확인합니다.
