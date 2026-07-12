# Issue #69 Manual QA

- Manual QA: 실제 애플리케이션·API·인프라 동작을 변경하지 않아 Level 5/6 수동 QA는 필요하지 않습니다.
- Adversarial QA: clean PR에서 Review·QA 댓글 0개인 상태로 `PRE_REVIEW_READY`에 도달하며, 미래 단계 입력·순환 의존·누락 링크·SHA 불일치는 계약 테스트가 거부합니다.
- Machine-readable QA: GitHub-shaped clean PR fixture를 실제 harness CLI에 입력하면 `PRE_REVIEW_READY`와 `DISPATCH_REVIEW_AND_QA`가 출력됩니다. Docs 등으로 head가 바뀌면 이전 initial Review와 QA가 모두 stale이며 한 역할만 fresh인 경우 후속 Docs/final Gate가 열리지 않습니다.
- Cleanup receipt: 로컬 애플리케이션, 컨테이너, 외부 관찰 도구를 시작하지 않았으므로 정리 대상이 없습니다.
- 미검증 항목: fresh Review, independent QA, Docs final sync와 최신 GitHub Actions는 PRE_REVIEW_READY 이후 역할이 수행합니다.
