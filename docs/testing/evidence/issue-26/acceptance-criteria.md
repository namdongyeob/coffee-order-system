# Issue #26 Acceptance Criteria

Issue: #26
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/26
Branch: `codex/issue-26-context-router`

## Execution Decision

Execution mode: STRICT
Execution mode reason: workflow policy와 harness 변경이며, 독립 Dev·QA·Review·Docs 검증 분리가 필요한 저장소 운영 변경입니다.
Level 5 required: NO
Level 5 reason: Java runtime 또는 애플리케이션 기동 경로를 변경하지 않았습니다.
Level 6 required: NO
Level 6 reason: API, HTTP, DB, 인프라 연결을 변경하지 않았고 실제 요청 경로가 없습니다.

## Completion Criteria and Recorded Evidence

- Context Router의 declared path 계약이 실제 Router에서 빈 목록(`[]`)일 때 통과합니다.
- temporary Router가 `missing.md`를 선언하면 harness CLI가 종료 코드 1로 실패하고 `missing.md`를 보고합니다.
- harness suite 48건과 독립 QA focused suite 48건이 통과했습니다.
- links-only worktree gate와 `git diff --check`이 통과했습니다. 후자의 CRLF 경고는 오류가 아닌 경고로만 관찰되었습니다.
- Review에서 발견한 Context Router 3~5개 count 모호성 및 actual Router contract test 누락은 Dev가 수정했습니다. 재시도 제한 차단 뒤 사용자가 승인한 wording-only 추가 재시도 후 구현 Review는 PASS입니다.
- Skill discovery는 PASS가 아닙니다. 새 read-only Codex session 시도는 `codex-cli 0.141.0`에서 configured `gpt-5.6-terra`가 더 새 CLI를 요구해 discovery 응답 전에 HTTP 400으로 중단되었습니다.

## Boundaries

- 이 Issue는 workflow policy/harness 변경입니다. Java runtime, API/HTTP, DB, infra 변경은 없습니다.
- 수동 API/DB/UI QA와 Level 5/6 검증은 적용 대상이 아닙니다.
