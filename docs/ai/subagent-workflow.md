# 서브에이전트 핸드오프 계약

이 문서는 역할별 입력·출력 계약만 정의합니다. 역할과 쓰기 권한은 [오케스트레이션 정책](orchestration-policy.md), Issue 실행 순서는 [Issue 실행 흐름](agent-rules.md), evidence 형식은 [Evidence Guide](../testing/evidence-guide.md)를 따릅니다.

| 역할 | 필수 입력 | 필수 출력 |
| --- | --- | --- |
| Dev Agent | Issue와 AC, execution mode와 reason, 직접 읽을 문서, 허용 쓰기 범위, 제외 범위, focused test, 마지막 Next Attempt | 변경 파일, 실행한 focused test, 결과, 미검증 Level과 이유, evidence 입력, 다음 Attempt |
| Combined Verifier | Dev diff, Issue와 AC, focused verification 명령, evidence 위치 | 독립 검토·검증 결과, 발견한 위험, Dev 반환 범위 또는 PASS, 다음 Attempt |
| Review Agent | Dev diff, Issue와 AC, Review Gate, 변경 영역의 직접 문서 | 요구사항·회귀·테스트·추상화·문서 품질 판정, Dev 반환 범위 또는 Follow-up Issue 후보 |
| QA Agent | Dev 결과, Issue evidence, 필요한 검증 명령과 환경 조건 | PASS/FAIL/BLOCKED/PARTIAL, 실행 결과, 미검증 Level과 이유, evidence 누락, Dev 반환 범위 또는 Follow-up Issue 후보 |
| Docs Agent | 확정된 검증 명령과 결과, Issue evidence 위치, 허용 문서 범위 | evidence와 verification log 반영 파일, 기록한 결과, 남은 위험 |
| Main Coordinator | 역할별 보고, evidence 존재 여부, CI 상태 | 선택 mode 충족 여부와 다음 역할에 전달할 최소 상태 |

## 최소 역할 packet 템플릿

```text
Issue URL: <live issue URL>
worktree 경로: <absolute path>
base SHA: <base SHA>
head SHA: <head SHA>
Acceptance Criteria: <short criteria>
필수 문서 경로: <3~5 canonical paths>
Diff 범위: <allowed paths and exclusions>
직전 P0/P1 finding: <finding or 없음>
```

모든 역할은 위 packet과 필요한 focused 명령, 마지막 `Next Attempt`만 전달합니다. source 본문, 전체 대화 fork, 전체 attempt-log, 전체 테스트 출력, `tasks/**/sources` 복사본을 전달하거나 만들지 않습니다. 역할은 필요할 때 worktree와 GitHub 정본을 직접 읽습니다.
