# Commands

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `gh issue create --title "코드 스타일 가이드(네이밍/DTO/테스트 명명) 문서화" ...` | Issue 생성 | https://github.com/namdongyeob/coffee-order-system/issues/106 |
| Explore agent(quick~medium) 실행 | DTO/네이밍/테스트/import/상수/예외/로깅 패턴이 실제로 일관되는지 조사 | 6개 패턴 일관 확인, 예외 처리·로깅은 불일치해 규칙 제외 대상으로 판정. |
| `Write docs/architecture/code-style-guide.md` | 신규 컨벤션 문서 작성 | 반영. |
| `Edit docs/ai/context-router.md` | Review hot path 필수 문서 목록에 새 문서 연결 | 반영. |
| `git add` + `git diff --cached > 임시 diff` | 독립 리뷰용 diff 추출(신규 파일 포함) | `%TEMP%/issue-106.diff` 생성. |
| Agent(general-purpose, fresh)로 독립 Combined Verifier 실행 | 문서의 모든 인용을 실제 소스와 대조 | PASS, 1건 사실 오류 지적(`XxxIntegrationTest` 위치 서술이 자기모순). |
| `head -3 OrderPaymentIntegrationTest.java`, `head -3 RankingRebuildServiceIntegrationTest.java` | 지적 사항 직접 재확인 | root vs `ranking.rebuild` 패키지로 서로 다름을 확인, 지적이 정확함. |
| `Edit docs/architecture/code-style-guide.md` | 위치 관련 서술 정정 | 반영. |
| `git checkout -b claude/issue-106-code-style-guide` | 하네스 브랜치 규칙에 맞춰 작업 브랜치 생성 | 완료. |
