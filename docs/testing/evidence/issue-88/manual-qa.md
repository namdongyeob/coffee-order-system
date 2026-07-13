# Manual QA

- 이 Issue는 `docs/product/questions-for-tutor.md`·`docs/testing/verification-matrix.md` 삭제, `docs/testing/test-strategy.md`·`docs/testing/evidence/orchestration-skill/baseline.md` 소규모 수정, `docs/ai/doc-lifecycle.md` 갱신만 변경합니다. 애플리케이션 코드, harness 스크립트는 변경하지 않았습니다.
- Level 5/6은 Issue 본문 결정대로 NO이며 runtime 또는 HTTP 검증을 완료로 표현하지 않습니다.
- 삭제 전 `git log --follow`로 두 문서의 실사용 이력을 직접 확인했습니다(이동 대안도 검토했으나, 실질적 보존 가치가 없다고 판단해 삭제를 선택했고 근거를 `attempt-log.md`에 남겼습니다).
- 삭제한 두 문서를 참조하는 다른 정본 문서가 없음을 삭제 전후로 grep 확인했습니다.
- Issue #36 산출물인 `docs/ai/doc-lifecycle.md`가 이번 조치로 stale해지지 않도록 obsolete/archive 표를 실제 결과로 함께 갱신했습니다.
- 문서 전용 Issue이므로 Gradle 빌드, runtime, API 테스트는 의도적으로 실행하지 않았습니다.
