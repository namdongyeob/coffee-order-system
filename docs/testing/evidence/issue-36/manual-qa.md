# Manual QA

- 이 Issue는 `docs/ai/doc-lifecycle.md`(신규) 작성과 `docs/ai/rule-source-map.md`의 정본 등록 1행만 변경합니다. 기존 정본 문서의 규칙 내용, 애플리케이션 코드, harness 스크립트는 변경하지 않았습니다.
- Level 5/6은 Issue 본문 결정대로 NO이며 runtime 또는 HTTP 검증을 완료로 표현하지 않습니다.
- Issue #36 자체의 "#58 완료 후 실행" 선행조건은 저장소 소유자가 세션에서 의도적으로 해제했습니다. `acceptance-criteria.md`와 `attempt-log.md`에 이 결정과 근거를 기록했습니다. 이 감사는 스냅샷이며 향후 문서 구조 개편 시 재감사가 필요할 수 있음을 `doc-lifecycle.md` 서두에 명시했습니다.
- obsolete 후보는 이 Issue에서 삭제하지 않았습니다. `questions-for-tutor.md`·`verification-matrix.md`는 이미 Issue #88로 분리했고, 새로 발견한 5개 후속 조치 후보(k6-plan.md·postman-guide.md 링크 보강, ADR-001·006 Router 등록, subagent-workflow.md 중복 정리, troubleshooting-log.md 빈 템플릿 처리)는 `doc-lifecycle.md`의 "후속 조치 후보" 절에 제안만 남기고 별도 Issue를 자동 생성하지 않았습니다(사용자 승인 후 생성 예정).
- 정적 미참조만으로 obsolete를 판정하지 않기 위해 `context-router.md`·`rule-source-map.md`뿐 아니라 `README.md`도 함께 확인했습니다. 이 과정에서 `erd.md`·`lecture-mapping.md`·`overview.md`가 AI hot path 밖이지만 README에는 링크된 오탐 사례를 실제로 확인했습니다.
- 문서 전용 Issue이므로 Gradle 빌드, runtime, API 테스트는 의도적으로 실행하지 않았습니다.
