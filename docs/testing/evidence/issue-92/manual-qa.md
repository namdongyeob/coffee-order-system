# Manual QA

- 이 Issue는 신규 문서 `docs/ai/team-merge-governance-baseline.md`와 3개 정본 문서의 참조 링크 1줄씩(`docs/ai/context-router.md`, `docs/ai/rule-source-map.md`, `docs/ai/orchestration-policy.md`), 그리고 `docs/testing/evidence/issue-92/` evidence 6개만 변경합니다. 애플리케이션 코드, harness 스크립트, GitHub 저장소 설정은 변경하지 않았습니다.
- Level 5/6은 Issue 본문 성격(문서 전용, GitHub 설정 미변경)에 따라 NO로 직접 판단했습니다.
- **GitHub 설정 미변경 확인**: 이 세션에서 `gh api`나 branch protection 관련 명령을 전혀 실행하지 않았습니다. 문서의 "활성화 체크리스트"는 전부 미체크(`- [ ]`) 상태로 두어, 실제 활성화가 아직 일어나지 않았음을 문서 자체가 표현합니다.
- **중복 방지**: 기존 `orchestration-policy.md`의 "merge 거버넌스" 절 내용(사람 도메인 오너 최종 승인, CI ground truth, 자율 큐 예외)을 새 문서에 복제하지 않고, 새 문서에서 `orchestration-policy.md`를 정본으로 링크했습니다. `rule-source-map.md`에도 "merge 거버넌스 기본값"과 "팀 이전 시 branch rule 구체 항목"을 별도 행으로 나눠 정본-참조 관계를 명시했습니다.
- **#56/#93 인용 정확성**: `gh issue view 56`으로 실제 본문을 읽고 신뢰 경계 문구를 그대로 인용했습니다(각색하지 않음). `gh issue view 93`으로 "현재 상태: 착수하지 않는다"를 확인하고 baseline 문서가 #93의 결과를 기다리지 않는다는 점을 명시했습니다.
- context-router.md의 "하네스 계약" 절이 요구하는 대로, 신규 문서에 대한 링크를 `context-router.md`(조건부 참조 문서 목록)에 추가해 `--check-links`가 검사할 수 있게 했습니다.
- Gradle 빌드, runtime, API 테스트는 이 Issue 범위(정책 문서)와 무관해 의도적으로 실행하지 않았습니다. 하네스 회귀(`pytest scripts/tests/`)만 문서 변경 전후로 실행해 영향 없음을 확인했습니다.
