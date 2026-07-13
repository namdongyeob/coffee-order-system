# 규칙 정본 지도

같은 규칙을 여러 문서에 복제하지 않습니다. 참조 문서는 아래 정본을 링크하고, 규칙을 변경할 때는 정본과 검사 스크립트만 함께 수정합니다.

| 규칙 영역 | 단일 정본 | 참조하는 문서·도구 |
| --- | --- | --- |
| 프로젝트 진입과 문서 라우팅 | `AGENTS.md` | `docs/ai/context-router.md`(문서 hot path 선택), `CLAUDE.md`(Claude 작업 진입, `AGENTS.md` 참조), 새 Codex 작업 |
| Issue 실행 순서와 기본 개발 흐름 | `docs/ai/agent-rules.md` | Issue/PR 템플릿 |
| 실행 모드 선택, 역할, 쓰기 권한, 모델 | `docs/ai/orchestration-policy.md` | `subagent-workflow.md`, Skill |
| 기계적 BLOCKED 판정 | `.codex/skills/coffee-order-issue-loop/SKILL.md` | Main Coordinator |
| 실제 LazyCodex 또는 하이브리드 적용 | `docs/ai/lazycodex-runbook.md` | 오케스트레이션 정책 |
| 검증 Level과 테스트 실행 소유권 | `docs/testing/test-strategy.md` | `qa-gate.md`, Skill |
| Review 판정 기준 | `docs/ai/review-gate.md` | `implementation-guardrails.md`, Context Router |
| QA 판정 기준 | `docs/ai/qa-gate.md` | `test-strategy.md`, Context Router |
| evidence 파일, Execution mode, Attempt 형식 | `docs/testing/evidence-guide.md` | Issue/PR 템플릿, harness gate |
| 완료 전 검사 목록 | `docs/ai/issue-completion-checklist.md` | PR 템플릿 |
| 검증 실행 결과 | `docs/testing/evidence/issue-{number}/verification.md` | Issue별 evidence와 on-demand 전역 뷰 |
| 반복 실수와 재발 방지 | `docs/ai/agent-mistakes.md` | 다음 Issue의 Plan 단계 |
| 프로젝트 Codex 권한 | `.codex/config.toml` | `docs/onboarding/codex-safety.md` |
| 문서 lifecycle 분류(active/conditional/archive/obsolete 후보) | `docs/ai/doc-lifecycle.md` | 없음(스냅샷 감사 결과, 후속 감사 시 재작성) |

## 변경 규칙

- 정본의 규칙을 바꾸면 이를 검사하는 테스트나 스크립트도 함께 확인합니다.
- 참조 문서에는 설명을 복사하지 않고 정본 경로와 그 문서를 읽어야 하는 조건만 남깁니다.
- 충돌하는 문구가 발견되면 위 표의 정본을 우선하고 참조 문서를 수정합니다.
