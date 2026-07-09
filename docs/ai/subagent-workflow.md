# 서브에이전트 워크플로우

## 목적

서브에이전트는 구현 속도를 높이기 위한 도구가 아니라, 역할을 분리해서 추측과 누락을 줄이기 위한 도구입니다.

## 표준 역할

| 역할 | 입력 | 출력 |
| --- | --- | --- |
| Spec Agent | 과제 발제, 설계안, 강의자료, 현재 문서 | 정책 결정표, 질문 목록, Issue 후보 |
| Dev Agent | 하나의 Issue, 관련 문서, 현재 코드 | 구현 코드, 테스트, 검증 로그 |
| Review Agent | PR diff, Issue, 관련 문서 | 리뷰 코멘트, 회귀 위험, 수정 필요 항목 |
| QA Agent | 실행 방법, 테스트 결과, 검증 로그 | 누락된 검증, 재현 절차, follow-up Issue |
| Docs Agent | 확정된 결정, 구현 결과, 검증 결과 | README, ADR, API 명세, runbook 갱신 |

## 사용 순서

```text
Spec Agent
  -> 사람이 범위 승인
  -> Dev Agent
  -> Review Agent
  -> Dev Agent 수정
  -> QA Agent
  -> Docs Agent
  -> 사람이 merge/close
```

## 병렬 사용 예시

- Review Agent와 QA Agent는 같은 diff를 읽고 서로 다른 관점으로 검토할 수 있습니다.
- Docs Agent는 Dev Agent 구현이 끝난 뒤 확정된 변경만 문서화합니다.
- Spec Agent는 다음 Issue 후보를 정리할 수 있지만, 현재 Issue 구현 범위를 바꾸면 안 됩니다.

## 금지 예시

- 두 Dev Agent가 같은 Service 또는 Entity를 동시에 수정합니다.
- 정책이 확정되지 않은 상태에서 Dev Agent가 구현을 시작합니다.
- QA Agent가 검증 누락을 발견하고 직접 코드를 수정합니다.
- Review Agent가 요구사항을 바꿔서 구현 범위를 넓힙니다.

## 완료 보고 형식

```text
역할:
대상 Issue:
읽은 문서:
수행한 작업:
검증:
미검증:
후속 Issue 후보:
```
