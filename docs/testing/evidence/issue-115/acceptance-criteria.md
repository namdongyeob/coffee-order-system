# Issue #115 Acceptance Criteria

Issue: #115
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/115

Execution mode: STANDARD
Execution mode reason: runtime은 바꾸지 않지만 README, ERD, API, 요구사항·범위의 여러 제출 정본을 최종 구현과 함께 동기화해 독립 일관성 검증이 필요합니다.
Level 5 required: NO
Level 5 reason: production 코드와 runtime 설정을 변경하지 않는 문서 전용 작업이며 최신 main의 실제 기동은 Issue #114에서 검증했습니다.
Level 6 required: NO
Level 6 reason: HTTP 계약을 새로 변경하지 않고 Issue #114에서 검증한 최종 API를 문서화합니다.

## Dev 완료 기준

- [x] clean `origin/main` SHA `b1e7732f3466a332b7f24a97e8b8c9a5a1867b23`에서 시작했습니다.
- [x] production·test·build·workflow 파일을 변경하지 않았습니다.
- [x] README에서 필수 API, 핵심 설계, ADR, ERD, API, 검증 evidence로 이동할 수 있습니다.
- [x] ERD를 Flyway V1~V7의 테이블·column·constraint·index와 비교해 동기화했습니다.
- [x] Outbox, `processed_event`, `ranking_event_ledger`, rebuild tables의 책임과 논리 관계를 설명했습니다.
- [x] API 명세를 현재 Redisson, Transactional Outbox, Kafka retry·DLT, Redis ranking, DLT replay·rebuild 계약과 맞췄습니다.
- [x] 요구사항·범위에서 Outbox 보류 표현을 제거하고 현재 완료·제외 범위를 구분했습니다.
- [x] 실제 Anyone-on-web TIL URL 5개를 README에 연결했습니다.
- [x] 공개 GitHub 저장소와 10개 이상 커밋 이력을 확인했습니다.
- [x] links-only gate, 문서 하네스 130건, 외부 TIL 링크 5개가 PASS했습니다.
- [x] Issue evidence와 PR body preflight에 필요한 metadata를 작성했습니다.

## Current disposition

PASS. Dev 문서 변경과 focused verification은 완료했습니다. 독립 Combined Verifier와 최신 PR-head CI는 PR 생성 뒤 후속 gate입니다.
