# Orchestration Skill Validation

## 검증 대상

- Global guidance: `%USERPROFILE%/.codex/AGENTS.md`
- Project guidance: `AGENTS.md`
- Project Skill: `.codex/skills/coffee-order-issue-loop/SKILL.md`
- Codex CLI: `0.144.0-alpha.4`
- Model: `gpt-5.6-luna`
- Reasoning: `low`
- Sandbox: `read-only`

## 새 세션 규칙 발견

프로젝트 루트에서 ephemeral Codex 세션을 시작해 로드된 규칙을 확인했습니다.

```text
한국어 기본: YES
한 Issue 범위: YES
production 코드 단일 작성자: YES
coffee-order-issue-loop 적용: YES
```

## 현재 계약 정적 검증

| 시나리오 | 기대 결과 | Skill 계약 | 판정 |
| --- | --- | --- | --- |
| Redisson, Kafka, Redis ZSET, DLT를 한 Issue에서 병렬 구현 | 기능별 Issue 분리, 구현 역할 미배정 | `BLOCKED: SPLIT ISSUES` | PASS |
| OrderService에 Dev Agent 둘 배정 | Dev Agent 한 명만 허용 | `BLOCKED: ONE WRITER` | PASS |
| Main이 작은 코드 수정 또는 테스트 실행 | 역할 재배정 | `BLOCKED: COORDINATOR ONLY` | PASS |
| Review가 발견한 문제를 직접 수정 | 원래 Dev에게 반환 | `BLOCKED: REVIEW READ ONLY` | PASS |
| QA가 독립 전체 테스트와 실제 환경 검증 | QA가 실행하고 Docs가 결과 기록 | Role Ownership에 명시 | PASS |
| 독립 Issue 두 개 병렬 실행 | 별도 worktree와 Dev Agent | Intake And Dispatch에 명시 | PASS |

`scripts/tests/test_harness_gate.py`가 Main 금지 계약, QA 독립 검증, 기존 `MAIN VERIFIES` 문구 제거를 검사합니다.

## 환경 차이

PowerShell `PATH`의 Codex CLI `0.141.0`은 `gpt-5.6-luna` 사용 시 최신 CLI가 필요하다는 400 응답을 반환했습니다. 데스크톱 앱이 사용하는 `0.144.0-alpha.4` 실행 파일에서는 동일 모델의 ephemeral 검증이 성공했습니다. 따라서 모델 접근 오류가 발생하면 계정 권한만 확인하지 말고 실제 실행 중인 CLI 버전도 확인해야 합니다.
