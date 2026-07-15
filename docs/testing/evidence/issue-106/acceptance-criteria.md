# Issue #106 Acceptance Criteria

Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/106

Execution mode: SOLO
Execution mode reason: 애플리케이션 동작, build, runtime을 바꾸지 않는 문서 전용 작업입니다.
Level 5 required: NO
Level 5 reason: 애플리케이션 런타임을 변경하지 않는 문서 전용 작업입니다.
Level 6 required: NO
Level 6 reason: 실제 API 계약을 변경하지 않는 문서 전용 작업입니다.

## 완료 기준

- [x] 새 문서(`docs/architecture/code-style-guide.md`)에 DTO record/`from` 팩토리, 패키지·클래스 네이밍, 테스트 네이밍, wildcard import 금지, 상수 네이밍 6개 패턴을 실제 코드 인용과 함께 문서화했습니다.
- [x] `docs/ai/context-router.md`의 Review hot path 필수 문서 목록에 새 문서를 연결했습니다.
- [x] 예외 처리(ApiException/ErrorCode)와 로깅(@Slf4j)은 실제로 모듈마다 갈려 있어 규칙으로 문서화하지 않고 "다루지 않는 것" 절에 근거와 함께 명시했습니다.

## 참고

- 독립 검증: general-purpose agent 1개(fresh)가 문서의 모든 인용을 실제 소스와 대조. 초안에서 1건의 사실 오류(`XxxIntegrationTest`가 항상 root 테스트 패키지에 위치한다는 서술이 `ranking/rebuild/RankingRebuildServiceIntegrationTest.java`의 실제 패키지(`ranking.rebuild`)와 모순)를 지적받아 위치는 규칙이 아니라고 정정했습니다. 나머지 항목은 전부 정확했습니다(PASS).
- `Current head`는 이 커밋을 가리킵니다: `084d2da2f66d4b3ef2100f4eaa39d0703ffbfab5`.
