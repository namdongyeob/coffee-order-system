# 서브에이전트 핸드오프

역할과 쓰기 권한의 정본은 `docs/ai/orchestration-policy.md`입니다. 이 문서는 역할 사이에 전달할 최소 컨텍스트만 정의합니다.

Issue 실행 순서는 `docs/ai/agent-rules.md`만 따릅니다.

## 필수 입력

- 대상 Issue 번호와 Acceptance Criteria.
- `Execution mode`와 비어 있지 않은 reason.
- 직접 읽어야 할 정본 문서 2~4개.
- 허용된 쓰기 파일 또는 모듈.
- 제외 범위.
- focused test 명령과 필요한 검증 Level.
- 이전 Attempt가 실패했다면 마지막 `Next Attempt` 입력.

전체 대화 fork, 전체 attempt-log, 전체 테스트 출력은 전달하지 않습니다. 재시도에도 Issue, AC, 직접 문서, write scope, test command, 마지막 `Next Attempt`만 사용합니다.

## 필수 출력

```text
역할:
대상 Issue와 execution mode:
읽은 문서:
허용된 쓰기 또는 검토 범위:
실행한 검증:
미검증 Level과 이유:
발견한 위험:
다음 역할에 전달할 내용:
```

역할 보고는 위 항목만 간결하게 작성합니다. 서브에이전트의 완료 문장만으로는 완료하지 않습니다. `STANDARD`와 `STRICT`에서 Main Coordinator는 선택된 mode의 필수 보고와 CI 상태가 모두 존재하는지만 확인하며, diff 내용 검토나 테스트 재실행은 하지 않습니다.
