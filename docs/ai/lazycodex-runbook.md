# LazyCodex 운영 절차

## 기본 원칙

LazyCodex는 한 번에 큰 기능을 통째로 구현하는 방식이 아니라, 작은 Issue를 반복 루프로 처리하는 방식으로 사용합니다.

```text
Specify -> Clarify -> Plan -> Implement -> Verify -> Review -> Document -> Next Issue
```

이 루프의 목적은 AI가 정책을 추측하지 않게 하고, 각 작업의 요구사항, 구현 범위, 검증 결과를 남기는 것입니다.

## Issue 루프

1. GitHub Issue를 만들거나 선택합니다.
2. Issue에 목표, 범위, 제외 범위, 완료 기준, 검증 레벨을 적습니다.
3. 격리된 worktree 또는 브랜치를 사용합니다.
4. Issue 본문과 관련 문서를 에이전트에게 제공합니다.
5. Dev Agent에게 해당 Issue만 구현하게 합니다.
6. Review Agent에게 diff 리뷰를 요청합니다.
7. QA Agent에게 검증 누락과 재현 절차를 확인하게 합니다.
8. 필요한 문서를 갱신합니다.
9. 사람이 merge와 close를 결정합니다.

## 서브에이전트 역할

| 역할 | 책임 | 금지 사항 |
| --- | --- | --- |
| Spec Agent | 요구사항, 정책 빈칸, Issue 분해를 정리합니다. | 구현 코드를 작성하지 않습니다. |
| Dev Agent | 하나의 Issue를 구현하고 테스트를 작성합니다. | 다른 Issue 범위를 건드리지 않습니다. |
| Review Agent | 요구사항 누락, 테스트 누락, 회귀 가능성, 과한 추상화를 검토합니다. | 명시 요청 없이 직접 수정하지 않습니다. |
| QA Agent | 검증 레벨, 재현 절차, 누락된 인프라 검증을 확인합니다. | 구현을 대신 고치지 않습니다. |
| Docs Agent | README, ADR, API 명세, runbook을 갱신합니다. | 코드 동작을 추측해서 문서화하지 않습니다. |

## 병렬 서브에이전트 사용 기준

서브에이전트는 작업 간 파일 충돌이 없고 결정 순서가 독립적일 때만 병렬로 사용합니다.

병렬 처리해도 되는 예시는 다음과 같습니다.

- Review Agent가 현재 diff를 검토하는 동안 Docs Agent가 변경된 API 문서 초안을 정리합니다.
- QA Agent가 검증 누락을 확인하는 동안 Spec Agent가 후속 Issue 후보를 정리합니다.
- 서로 다른 문서 파일을 대상으로 하는 docs 작업을 분리합니다.

병렬 처리하지 않는 예시는 다음과 같습니다.

- 같은 서비스 코드를 두 Dev Agent가 동시에 수정하는 경우.
- 도메인 정책이 확정되지 않았는데 구현과 문서를 동시에 진행하는 경우.
- DB 스키마 변경과 API 구현처럼 순서 의존성이 큰 경우.

## 루프별 산출물

| 단계 | 산출물 |
| --- | --- |
| Specify | 요구사항 요약, MVP 범위, 제외 범위. |
| Clarify | 튜터 질문, 정책 빈칸, ADR 후보. |
| Plan | 구현 순서, 영향 파일, 검증 계획. |
| Implement | 코드, 테스트, 최소 문서 갱신. |
| Verify | `docs/testing/verification-log.md` 기록. |
| Review | 리뷰 코멘트, 회귀 위험, 후속 Issue 후보. |
| Document | README, API 명세, ADR, runbook 갱신. |

## 에이전트에게 줄 기본 프롬프트 형식

```text
AGENTS.md를 먼저 읽어라.
대상 Issue 하나만 처리해라.
관련 문서는 다음이다.
- docs/...

범위 밖 작업은 하지 마라.
정책이 불명확하면 구현하지 말고 질문으로 남겨라.
완료 주장은 검증 로그를 남긴 뒤에만 해라.
```

## 이번 프로젝트 적용 순서

1. Spec Agent가 문서와 Issue 초안을 정리합니다.
2. 사람이 Issue 우선순위를 승인합니다.
3. Dev Agent가 `[feat] 커피 메뉴 목록 조회 API`부터 구현합니다.
4. Review Agent가 PR 또는 diff를 검토합니다.
5. QA Agent가 검증 레벨과 누락된 실제 실행 검증을 확인합니다.
6. 사람이 merge 또는 follow-up Issue 생성을 결정합니다.

## 현재 프로젝트 루트

`C:\Users\user\Desktop\Commerce-live-chat-system-Agora\.claude\worktrees\coffee-order-system`

## 주의

현재 프로젝트는 Claude worktree 경로 안에 있습니다. Git remote와 branch 상태를 항상 명시합니다.
