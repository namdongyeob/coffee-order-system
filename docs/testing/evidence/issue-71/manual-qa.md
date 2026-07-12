# Issue #71 Manual QA

- 작업 유형은 workflow policy와 harness Level 0 계약입니다.
- focused 행위 계약 28 tests와 전체 harness 76 tests로 미래 역할 링크 없는 pre-review, STRICT 역할 수, 고정 docs-only allowlist, non-doc QA stale, 조건부 merge와 다음 Issue 진입 조건을 검증했습니다.
- adversarial 계약은 screenshot 하위 경로, png, raw output, 임의 Markdown, 다른 Issue evidence, policy, scripts, src, test, build, workflow, runtime 경로를 모두 QA stale로 판정합니다.
- 저장소 gate와 링크 검사, diff 정적 검사, 저장소 밖 UTF-8 no-BOM 한국어 PR body preflight를 검증했습니다.
- Level 5와 Level 6은 모두 NO이므로 애플리케이션, 컨테이너, HTTP/API/UI 수동 검증은 실행하지 않았습니다.
- GitHub가 소유하는 현재 head, Review·QA·CI, merge 상태는 이 repository evidence에 복제하지 않습니다.
