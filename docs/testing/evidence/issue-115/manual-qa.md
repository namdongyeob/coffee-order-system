# Issue #115 Manual QA

Issue: #115
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/115
Date: 2026-07-18

## 읽은 문서와 역할

- Dev Agent 한 명이 문서와 evidence의 유일한 writer였으며 구현 subagent는 사용하지 않았습니다.
- `AGENTS.md`, coffee-order issue loop, Context Router와 Docs/evidence hot path의 evidence guide, rule source map, completion checklist, orchestration policy를 읽었습니다.
- 수정 대상 README, ERD, API 명세, 요구사항·범위와 Flyway V1~V7을 직접 비교했습니다.

## Manual QA

- README에서 요구사항, 범위, API, ERD, ADR-001~008, 운영, 테스트, Issue #114 evidence로 이동하는 내부 링크를 확인했습니다.
- ERD의 9개 테이블과 V1~V7의 primary/unique/check/foreign key/index를 대조했습니다.
- Outbox는 발행 대기, `processed_event`는 consumer 호환 이력, `ranking_event_ledger`는 Redis projection 복구 원장이라는 서로 다른 책임을 명시했습니다.
- 단일 통합 트러블슈팅 페이지는 Coordinator가 Notion Share UI에서 상속된 `Anyone on the web with link / Can view`를 확인한 URL이며, 이 worktree에서 HTTP 200임을 재확인했습니다.
- GitHub repository visibility가 `PUBLIC`이고 커밋 이력이 10개 이상임을 확인했습니다.

## Adversarial QA

- Outbox를 아직 보류라고 표현한 문구와 Redisson·Kafka 발행을 미래 작업으로 적은 API 문구를 제거했습니다.
- `processed_event`를 Redis exactly-once의 단독 근거로 오해하지 않도록 ranking ledger·Lua marker와 책임을 분리했습니다.
- retention이 모든 ledger나 Redis marker를 SCAN 삭제한다고 표현하지 않고, 오래된 적격 `COMMITTED` DB 행의 bounded cleanup과 pending 보존을 명시했습니다.
- Notion Sites 공개 URL을 추측하지 않고 실제 제공된 Anyone-on-web URL만 사용했습니다.
- Review 뒤 `ranking_rebuild_run_event.event_type` check를 Flyway V6와 ERD에서 다시 대조했습니다.
- Redis 복구는 DB에서 재생하는 것이 아니라 Kafka replay로 temp ZSET을 만들고 DB 주문 집계로 검증한다는 경계를 README와 API에서 일치시켰습니다.

## Cleanup receipt

- Docker·애플리케이션·Gradle 장기 프로세스를 시작하지 않았습니다.
- 임시 container, listener, volume을 만들지 않았습니다.

## 미검증 항목과 남은 위험

- 독립 Combined Verifier와 최신 PR-head GitHub CI는 PR 생성 뒤 pending입니다.
- 통합 트러블슈팅 URL은 HTTP 200과 Share UI 확인 근거를 사용했으며 익명 브라우저의 전체 본문 렌더링은 이 Dev 세션에서 반복하지 않았습니다.
