# Issue #36 Acceptance Criteria

Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/36

Execution mode: STANDARD
Execution mode reason: 애플리케이션 동작과 workflow 정책을 변경하지 않고 문서 사용성과 참조 관계를 감사하는 문서 작업입니다.
Level 5 required: NO
Level 5 reason: 애플리케이션 런타임을 변경하지 않습니다.
Level 6 required: NO
Level 6 reason: HTTP API 계약과 요청 흐름을 변경하지 않습니다.

## 선행조건 해제 결정

Issue #36의 두 번째 댓글은 "이 Issue는 개정 라운드 마지막(#58 완료 후)에 실행합니다"라고 명시했습니다. 저장소 소유자(사용자)는 세션 대화에서 "#57·#58은 팀 프로젝트로 넘어가서 실사용 문제가 생기면 그때 처리하고, 그때 맞춰 문서 구조를 다시 손보면 된다"는 이유로 이 선행조건을 의도적으로 해제하고 지금 #36을 진행하기로 결정했습니다. #57·#58은 이번 세션에서 별도로 보류(미구현)로 유지됩니다. 이 감사는 스냅샷이며, #58 이후 문서 구조가 바뀌면 재감사가 필요할 수 있습니다.

## 완료 기준

- [x] docs 전체 인벤토리와 분류 결과가 있습니다. `docs/ai/doc-lifecycle.md`에 non-evidence 문서 54개 + evidence 디렉터리(약 200개 파일, 일괄 archive 처리) + root 진입점 3개를 active/conditional/archive/obsolete 후보로 분류했습니다.
- [x] 각 분류에 실제 참조 경로 또는 보존 근거가 기록됩니다. `context-router.md`, `rule-source-map.md`, `README.md` 세 경로의 실제 grep 결과를 근거로 기록했습니다.
- [x] Router·정본 지도에서 도달하지 못하는 문서가 식별됩니다. `doc-lifecycle.md`의 "Router·정본 지도 도달성 문제 요약" 절에 ADR-001, ADR-006, k6-plan.md, postman-guide.md, lazycodex-runbook.md의 도달성 문제와 erd.md/lecture-mapping.md/overview.md의 README-only 오탐 사례를 기록했습니다.
- [x] archive와 obsolete 후보가 명확히 구분됩니다. archive(보존, 삭제 대상 아님) 6개 항목과 obsolete 후보(삭제 검토 필요) 4개 항목을 별도 표로 분리했습니다.
- [x] 삭제·통합이 필요한 항목은 별도 후속 Issue 후보로 정리됩니다. `questions-for-tutor.md`·`verification-matrix.md`는 이미 Issue #88로 분리했고, `doc-lifecycle.md`의 "후속 조치 후보" 절에 이번 감사로 새로 발견한 5개 항목(링크 보강 2건, Router 조건부 목록 추가 2건, 중복 정본 정리 1건, 빈 템플릿 처리 1건)을 별도 Issue로 제안만 남기고 자동 생성하지 않았습니다.
- [x] 기본 evidence 파일과 `docs/testing/evidence/issue-36/verification.md`가 있습니다.

검증 실행 head는 `f8756fb`입니다. fresh 독립 Combined Verifier는 head `1fe372e`에서 `docs/adr/README.md` 분류 누락 1건을 지적해 `REVISE`를 반환했고, `f8756fb`에서 정정한 뒤 최종 재검토를 진행합니다.
