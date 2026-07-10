# Issue #23 Acceptance Criteria

Execution mode: STRICT
Execution mode reason: 하네스와 워크플로 정책을 변경하므로 독립 Review, QA, Docs 및 CI가 필요한 운영 계약 작업입니다.
Level 5 required: NO
Level 5 reason: Java 애플리케이션의 런타임 동작을 변경하지 않는 문서·저장소 운영 작업입니다.
Level 6 required: NO
Level 6 reason: 실제 호출 대상 API가 추가되거나 변경되지 않는 문서·저장소 운영 작업입니다.

## 완료 조건

- 규칙의 단일 정본과 참조 관계가 문서화됩니다.
- SOLO, STANDARD, STRICT의 선택 근거와 모드별 dispatch·완료 규칙이 문서화됩니다.
- Attempt 실패가 다음 Attempt 입력으로 연결됩니다.
- Level 5/6 결정이 Issue와 PR 템플릿 및 로컬 검사에 포함됩니다.
- 프로젝트 권한, Git 훅, PR 검사, GitHub Actions가 재현 가능한 명령으로 제공됩니다.
- Java production/test 파일은 변경하지 않습니다.
