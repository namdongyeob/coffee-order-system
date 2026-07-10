# Issue #23 Attempt Log

Issue: #23
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/23
Branch: codex/issue-23-harness-quality-gates

## Attempt 1

### Generate

- Dev Agent에게 문서 정본, 권한, 훅, 검사 스크립트, CI 구현을 위임했습니다.

### Evaluate

- FAIL. 제한 시간과 상태 요청 이후에도 완료 보고와 검증 결과가 없었습니다.

### Failure Cause

- 격리된 서브에이전트 작업이 장시간 실행됐고 검증 가능한 self-report가 반환되지 않았습니다.

### Change Scope

- 업로드된 초안 중 `.codex/config.toml`, `.githooks`, workflow, Python 스크립트만 TDD와 Main 검증을 거쳐 재사용합니다.
- Java production/test 코드는 수정하지 않습니다.

### Reverification

- 초기 `unittest` 실행 결과 10건 중 10건이 실패 또는 오류로 RED를 확인했습니다.

### Next Attempt

- Main Agent가 테스트 계약을 기준으로 스크립트를 수정하고 문서 정본과 evidence를 완성합니다.

## Attempt 2

### Generate

- `harness_gate.py`를 테스트 계약에 맞게 수정하고 정본 지도, 템플릿, 훅·CI 문서를 연결했습니다.

### Evaluate

- PASS. 하네스 테스트 10건, repository gate, Git hooks, Java compile, 전체 Gradle 테스트가 통과했습니다.

### Failure Cause

- 없음. 최종 검증 전입니다.

### Change Scope

- Issue #23에서 승인된 문서, `.codex`, `.githooks`, `.github`, `scripts` 범위만 수정합니다.

### Reverification

- `python -m unittest discover -s scripts/tests -p "test_*.py"`: 10건 PASS.
- `python scripts/harness_gate.py --issue 23 --base-ref origin/main --check-links`: PASS.
- `git hook run pre-commit`, `git hook run pre-push`: PASS.
- `.\gradlew.bat compileJava --no-daemon`: PASS.
- `.\gradlew.bat test --no-daemon`: 최초 Docker daemon 미가동 FAIL, daemon 시작 후 동일 명령 PASS.

### Next Attempt

- 독립 Review 결과를 확인합니다.

## Attempt 3

### Generate

- 프로젝트 `.codex/config.toml`이 전역 권한을 덮어쓰는지 새 ephemeral Codex 세션으로 확인했습니다.

### Evaluate

- FAIL. 세션 로그가 `approval: never`, `sandbox: danger-full-access`를 표시했습니다.

### Failure Cause

- 현재 데스크톱 CLI `0.144.0-alpha.4` 환경에서 저장소 `.codex/config.toml`이 실제 실행 설정으로 적용되지 않았습니다.

### Change Scope

- 전역 설정은 임의로 변경하지 않습니다.
- CLI 옵션으로 `on-request / workspace-write`를 요청하고 권한 덮어쓰기 인자를 거부하는 `scripts/start_codex_workspace.ps1`와 제한 문서를 추가합니다.

### Reverification

- 제한 래퍼 세션에서 `sandbox: workspace-write`를 확인했습니다.
- `approval: never`가 유지되어 승인 정책은 강제되지 않았습니다.

### Next Attempt

- approval 제한은 알려진 환경 제약으로 남기고 독립 Review를 확인합니다.

## Attempt 4

### Generate

- 독립 문서·도구 Review가 발견한 템플릿 모호성, 중복 흐름, 빈 evidence 통과, 링크 파서, 훅 우회, Linux Gradle 권한, Codex 래퍼 명칭과 인자 덮어쓰기 문제를 수정했습니다.

### Evaluate

- PASS. 강화된 하네스 단위 테스트 16건, repository gate, YAML 파싱, branch guard, 전체 Gradle 테스트가 통과했습니다.

### Failure Cause

- 초기 구현은 사람이 읽을 때는 의도를 알 수 있었지만, `YES / NO` 같은 placeholder와 제목만 있는 evidence도 기계 검사를 통과할 수 있었습니다.
- Ubuntu runner의 `gradlew` 실행 권한과 Codex 래퍼의 권한 인자 덮어쓰기도 명시적으로 막지 않았습니다.

### Change Scope

- Issue/PR 템플릿, 정본 참조 문서, 하네스 스크립트·테스트, Git 훅, workflow, Codex 실행 래퍼만 수정했습니다.
- Java production/test 코드는 수정하지 않았습니다.

### Reverification

- `python -m unittest discover -s scripts/tests -p "test_*.py"`: 16건 PASS.
- `python scripts/harness_gate.py --issue 23 --base-ref origin/main --check-links --include-worktree`: PASS.
- workflow YAML parse: PASS.
- `scripts/start_codex_workspace.ps1 --sandbox=danger-full-access`: 의도된 거부, 종료 코드 2.
- `scripts/start_codex_workspace.ps1 -C C:\`: 의도된 거부, 종료 코드 2.
- `python scripts/harness_gate.py --check-branch --branch main`: 의도된 거부, 종료 코드 1.
- `.\gradlew.bat test --no-daemon`: PASS, 31초.

### Next Attempt

- 최종 독립 Review 결과를 반영하고 PR CI를 확인합니다.

## Attempt 5

### Generate

- Main Coordinator가 직접 구현·코드리뷰·테스트·commit·push·merge하지 않는 역할 구조로 정책, Skill, 테스트 전략을 변경했습니다.
- 독립 Issue는 별도 worktree에서 병렬 Dev를 허용하고, Dev 완료 후 Review와 QA를 병렬 배정하도록 했습니다.

### Evaluate

- 최초 Review는 `AGENTS.md`의 단일 Issue 문구와 병렬 Issue 규칙 충돌, 역할 정본 중복, 계약 테스트 범위 부족을 FAIL로 판정했습니다.
- 수정 후 Review Agent는 역할 경계에 PASS를 반환했고 QA Agent는 하네스 테스트 17건, repository gate, diff check를 모두 PASS로 판정했습니다.

### Failure Cause

- 기존 설계는 Main이 최종 검증을 모두 소유해 병목이 됐고, 역할 설명이 정책·Skill·테스트 전략에 반복되어 변경 시 불일치가 생겼습니다.

### Change Scope

- 역할과 쓰기 권한 정본은 `orchestration-policy.md`, 테스트 실행 정본은 `test-strategy.md`, 기계적 BLOCKED 판정은 Skill로 제한했습니다.
- Java production/test 코드는 수정하지 않았습니다.

### Reverification

- Review Agent: PASS. Main 제한, QA 독립 검증, 독립 Issue 병렬 worktree를 확인했습니다.
- QA Agent: 하네스 테스트 17건 PASS, repository gate PASS, `git diff --check` PASS.

### Next Attempt

- 변경을 PR #24에 push하고 GitHub Actions 결과를 확인합니다.

## Attempt 6

### Generate

- `SOLO`/`STANDARD`/`STRICT`를 적응적으로 선택하고, 첫 Dev가 stalled 상태가 된 뒤 replacement Dev를 재배정했습니다.

### Evaluate

- 최초 Review는 SSOT, PR body validation, hard concurrency wording, duplicate declarations를 지적했습니다.
- Dev가 지적 사항을 수정했습니다.
- QA는 28개 테스트, harness, diff, YAML 검증을 PASS로 판정했습니다.
- Reviewer는 exact slot wording을 포함한 runtime Skill gate를 승인했습니다.

### Failure Cause

- 최초 결과의 SSOT와 PR body 검증이 불충분했고, 동시성 제한 문구와 역할 선언이 중복되었습니다.

### Change Scope

- 오케스트레이션 정책과 Issue #23 evidence 문서만 반영했습니다. Java 변경은 없습니다.

### Reverification

- QA: 28개 테스트, harness, diff, YAML PASS.
- Reviewer: runtime Skill gate와 exact slot wording PASS.

### Next Attempt

- push 후 CI 결과를 확인합니다.
