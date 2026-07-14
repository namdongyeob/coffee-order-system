# Manual QA

- 이 Issue는 `scripts/team_orchestration.py`(신규 도구), `scripts/tests/test_team_orchestration.py`(신규 테스트), `.gitignore`(1줄 추가) 3개만 변경합니다. 애플리케이션 코드, `docs/` 정본, 기존 `scripts/harness_gate.py`는 변경하지 않았습니다.
- Level 5/6은 Issue 본문·acceptance-criteria.md 결정대로 NO이며 runtime 또는 HTTP 검증을 완료로 표현하지 않습니다.
- **재사용성**: `team_orchestration.py`는 `harness_gate.py`를 import하지 않습니다(콘솔 인코딩 하드닝 함수만 같은 패턴을 복제 — 다음 프로젝트로 파일 하나만 옮겨도 동작하도록 의도적으로 독립시켰습니다).
- **슬롯 한도**: `max_active_agents` 기본 3, `max_writer_agents` 기본 2. `max_writer_agents`를 3으로 설정하면 `TeamOrchestrationConfig.__post_init__`이 즉시 `TeamOrchestrationError`를 던져 3-Dev 예외가 애초에 구성 불가능합니다(런타임 검사가 아니라 생성 시점 차단). `resolved()`가 `os.cpu_count()` 기반으로 자동 축소합니다.
- **owned paths 충돌**: 디렉터리 포함 관계 휴리스틱(`owned_paths_overlap`)으로 같은 Service를 겹치게 소유하려는 두 번째 등록을 `SCOPE_CONFLICT`로 막습니다. "menu"와 "menuitem"처럼 문자열 접두사만 같고 실제로는 다른 디렉터리인 경우는 겹치지 않는다고 정확히 판정합니다(`test_shared_prefix_but_different_directory_does_not_overlap`).
- **메시지 4종만 허용**: `MESSAGE_TYPES`가 FINDING/NEED/BLOCKED/SCOPE_CONFLICT로 고정되어 있고, 등록되지 않은 agent나 목록 밖 타입은 예외를 던집니다.
- **gitignored runtime state**: `.team-orchestration-state/`를 `.gitignore`에 추가했고, `test_gitignored_state_directory_is_declared_in_repository_gitignore`로 실제 저장소 `.gitignore` 파일을 읽어 확인합니다. `git status --short`로도 상태 디렉터리가 노출되지 않음을 수동 확인했습니다.
- **자동 merge·autofix 부재**: `NoAutoMergeInvariantTest`가 모듈 소스에서 merge/rebase/cherry-pick/autofix 관련 함수명이나 `git merge` 서브커맨드 문자열이 없음을 스캔하고, `metrics_snapshot()`의 `merge_conflicts`가 항상 0으로 고정됨(실제 병합 로직이 없어 병합 충돌 자체가 발생할 수 없음)을 확인합니다.
- **합성 smoke(속도 주장 아님)**: `SyntheticSmokeTest`가 menu/point 비중첩 합성 Dev 작업 2개를 등록→메시지 교환→해제까지 통과시키고, 겹치는 합성 작업 2개는 SCOPE_CONFLICT로 막히는 반례도 확인합니다. Issue 본문 "측정 방식" 절이 요구한 대로 이 결과를 속도·토큰 효과 증거로 사용하지 않는다는 문구를 이 파일과 PR 본문에 명시합니다.
- 실전 속도·토큰 효과 측정은 Issue 본문이 지정한 대로 모의투자 프로젝트에서 별도로 수행합니다. 이 Issue는 그 측정을 수행하지 않습니다.
- Review/QA 병렬 실험(2차)과 Dev 3명 예외는 Issue 본문이 범위 밖으로 명시해 구현하지 않았습니다.
- Gradle 빌드, runtime, API 테스트는 이 Issue 범위(Python CLI 도구)와 무관해 의도적으로 실행하지 않았습니다.
