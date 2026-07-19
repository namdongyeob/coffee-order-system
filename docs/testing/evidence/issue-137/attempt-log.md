# Issue Attempt Log

Issue: #137
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/137
Branch: codex/issue-137-harness-lightweight
Current disposition: PASS
Current Attempt: 4
Current head: 804f651b2928791b4cec8e05978e2b194a9ca774

## Attempt 1

### Generate

- Issue 본문과 직접 정본 5개를 기준으로 영향도 분류·stale·auto-merge·CI·packet 계약 테스트를 먼저 작성했습니다.
- 단일 분류기와 workflow·evidence·orchestration 정본을 최소 구현했습니다.
- 최초 Generate 시각은 실시간 기록하지 않아 작업 시간은 추정하지 않습니다.

### Evaluate

- 새 focused suite는 구현 전 missing API와 기존 workflow 중복 때문에 예상한 RED를 보였습니다.
- 구현 뒤 focused 13개와 전체 scripts 177개가 PASS했습니다.
- 한 부분 재실행은 `scripts/tests`를 workdir로 잘못 지정해 import error가 났고 저장소 루트에서 같은 대상 명령으로 즉시 교정해 PASS했습니다.

### Failure Cause

- 기능 blocker는 없습니다. 위 import error는 구현 결함이 아니라 잘못된 명령 workdir이 원인이었습니다.

### Change Scope

- 허용된 harness, 직접 tests, workflow, AGENTS 진입점, 직접 관련 orchestration/test/evidence 정본만 변경했습니다.
- `src/main/**`, `src/test/**`, migration, production API·Kafka·Redis·Docker와 #132 evidence는 변경하지 않았습니다.

### Reverification

- `python -m unittest scripts.tests.test_harness_gate_issue_137`: 13 tests PASS.
- `python -m unittest discover -s scripts/tests -p "test_*.py"`: 177 tests PASS.
- repository gate(`--check-links --check-branch --include-worktree`): PASS.
- 실제 PR diff 영향도: `STRICT`, Java CI false, Review·QA stale true, runtime evidence stale false.
- `git diff --check`: PASS.

### Next Attempt

edited/source concurrency 분리, rename/delete stale 보존, optional evidence 모순 차단 P1을 Attempt 2에서 수정합니다.

## Attempt 2

### Generate

- edited run의 check 이름과 concurrency를 `metadata-gates`로 분리해 source `quality-gates`를 취소·대체하지 못하게 했습니다.
- post-QA stale helper가 path 문자열 대신 `ChangeRecord`를 받아 rename/delete status를 분류기까지 보존하게 했습니다.
- #138 이후 optional Attempt·metrics가 존재하면 disposition·head·retry reconciliation을 적용했습니다.

### Evaluate

- 세 P1별 focused fixture가 변경 전 각각 예상한 RED를 보였습니다.
- 최소 수정 뒤 P1 fixture, 최종 focused 17개와 전체 scripts 181개가 PASS했습니다.

### Failure Cause

- P1 #1은 source와 edited가 동일 concurrency와 check 이름을 공유한 것이 원인이었습니다.
- P1 #2는 helper가 name-status를 path 문자열과 합성 `M`으로 축소한 것이 원인이었습니다.
- P1 #3은 경량 분기에서 optional Attempt가 있어도 lightweight reconciliation만 호출한 것이 원인이었습니다.

### Change Scope

- workflow, harness, 직접 contract tests와 관련 orchestration/test/evidence 정본만 수정했습니다.
- production, application test, Gradle, Docker, Level 3~7과 #132는 변경하지 않았습니다.

### Reverification

- `python -m unittest scripts.tests.test_harness_gate_issue_137`: 17 tests PASS.
- `python -m unittest discover -s scripts/tests -p "test_*.py"`: 181 tests PASS.
- final evidence 상태의 repository gate와 `git diff --check`: PASS.
- Gradle·Docker·Level 3~7은 실행하지 않았습니다.

### Next Attempt

없음. 새 final head에서 fresh Review·QA와 source `quality-gates` CI를 다시 확인합니다.

## Attempt 3

### Generate

- #137 bootstrap source run은 새 경량 규칙을 소급하지 않고 전체 Gradle을 실행하며 #138부터 영향도 분류를 적용하도록 경계를 분리했습니다.
- execution-head delta는 `git diff --name-status --find-renames`의 `ChangeRecord`를 유지하고 current-Issue evidence A/M만 허용하도록 변경했습니다.
- merge helper는 같은 SHA의 고정 `quality-gates: SUCCESS`만 인정하고 `metadata-gates`를 거부하도록 변경했습니다.
- `ready_for_review`를 source trigger에서 제거해 동일 SHA Gradle 재실행을 막았습니다.

### Evaluate

- 네 P1 fixture는 구현 전 각각 missing bootstrap helper, 문자열 축소, CI identity 누락, 중복 trigger로 RED였습니다.
- 최소 수정 뒤 네 fixture, focused 20개와 전체 scripts 184개가 PASS했습니다.

### Failure Cause

- #137과 #138 이후 경계가 Java CI 출력에 반영되지 않았습니다.
- execution-head 경로가 `--name-only` 문자열로 축소되어 rename/delete 상태를 잃었습니다.
- merge helper가 CI boolean과 head만 받아 source check identity를 구분하지 못했습니다.
- `ready_for_review`가 source event에 포함되어 동일 SHA의 비싼 Gradle을 다시 만들 수 있었습니다.

### Change Scope

- workflow, harness, 직접 contract tests, 네 직접 정책 정본과 Issue #137 evidence만 수정했습니다.
- `src/**`, DB·Kafka·Redis·Docker 동작, #132와 범위 밖 기능은 변경하지 않았습니다.

### Reverification

- `python -m unittest scripts.tests.test_harness_gate_issue_137`: 20 tests PASS.
- `python -m unittest discover -s scripts/tests -p "test_*.py"`: 184 tests PASS.
- #137 impact는 Java CI true, #138 동일 경량 영향도는 false이며 repository gate와 `git diff --check`가 PASS했습니다.
- 로컬 Gradle은 동일 입력 재실행 금지에 따라 실행하지 않고 최종 source `quality-gates`의 setup-java와 Gradle step을 확인합니다.

### Next Attempt

없음. 새 final head를 확정해 fresh Review·QA에 넘기고 source `quality-gates`의 setup-java·Gradle SUCCESS를 확인합니다.

## Attempt 4

### Generate

- 포괄 `scripts/` 경로 분류를 제거하고 앱 runtime을 실행하지 않는 현재 repository 도구와 직접 테스트만 정확한 파일 allowlist로 고정했습니다.
- `scripts/replay_dlt_message.ps1`은 runtime-heavy로, allowlist 밖 신규 `scripts/**`는 unknown fail-closed로 분류했습니다.
- replay script 본문과 기존 네 P1 구현은 변경하지 않았습니다.

### Evaluate

- exact allowlist 상수 부재, replay·신규 ops 경량 오분류 fixture가 구현 전 예상 RED였습니다.
- mixed·rename/delete heavy fixture는 기존 보호가 이미 GREEN이었고 최소 수정 뒤 네 focused fixture가 모두 PASS했습니다.
- 최종 focused 24개와 전체 scripts 188개가 PASS했습니다.

### Failure Cause

- `_impact_category`의 포괄 `scripts/` prefix가 runtime 도구와 미등록 신규 스크립트까지 workflow-harness-policy로 축소한 것이 원인이었습니다.

### Change Scope

- `scripts/harness_gate.py`, 직접 Issue #137 fixture와 evidence만 수정했습니다.
- workflow, 정책 정본, production/API/DB/Kafka/Redis/Docker, replay script 본문과 기존 네 P1은 변경하지 않았습니다.

### Reverification

- `python -m unittest scripts.tests.test_harness_gate_issue_137`: 24 tests PASS.
- `python -m unittest discover -s scripts/tests -p "test_*.py"`: 188 tests PASS.
- repository gate와 `git diff --check`: PASS.
- 새 final head의 source `quality-gates` 결과에서 Gradle step 상태를 확인합니다.

### Next Attempt

없음. 정확 파일 allowlist와 unknown/runtime fail-closed만 구현한 새 head를 fresh Review·QA로 넘깁니다.
