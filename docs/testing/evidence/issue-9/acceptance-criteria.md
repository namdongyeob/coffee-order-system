# Issue #9 Acceptance Criteria

Issue: #9
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/9
Branch: codex/issue-9-redis-ranking-write

Execution mode: STRICT
Execution mode reason: Redis ZSET 쓰기와 runtime 인프라 연결을 변경하므로 독립 Dev, Review, QA, Docs 검증과 CI가 필요합니다.
Level 5 required: YES
Level 5 reason: 실제 애플리케이션과 Redis runtime 연결 및 health를 확인해야 합니다.
Level 6 required: NO
Level 6 reason: 외부 HTTP API를 추가하거나 변경하지 않고 내부 Redis 랭킹 쓰기 서비스만 구현합니다.

## 완료 조건

- [x] 입력 `menuId`와 `orderedAt`으로 랭킹을 증가시킵니다.
- [x] key는 `popular:menus:{yyyy-MM-dd}`이며 날짜는 `orderedAt` 기준입니다.
- [x] member는 `menuId`의 문자열 표현입니다.
- [x] Redis ZSET에 `ZINCRBY key 1 member`와 동등한 원자적 증가를 수행합니다.
- [x] key 생성 규칙은 한 곳에서 관리합니다.
- [x] 단위 테스트로 key와 member 변환 규칙을 검증합니다.
- [x] Level 4 실제 Redis Testcontainers에서 같은 입력 2회, 다른 날짜, 다른 메뉴의 score와 key/member 분리를 검증합니다.
- [x] Level 1 전체 회귀를 통과합니다.
- [x] Level 5 애플리케이션과 Redis runtime 기동 및 health를 확인합니다.
- [x] Level 6은 HTTP API 변경이 없어 실행하지 않습니다.
- [x] Kafka Consumer/group, `processed_event`, retry/DLT, replay/rebuild, Top 3 조회, TTL, 공통 Redis facade/framework를 구현하지 않습니다.

## Prospective timing

- Attempt 1 start: `2026-07-11T12:13:43.222+09:00`.
- Attempt 1 end: `2026-07-11T12:16:47.511+09:00`.
- Attempt 1 duration: `184.290s`.
- Attempt 2 start: `2026-07-11T12:21:30.914+09:00`.
- Attempt 2 end: `2026-07-11T12:29:51.008+09:00`.
- Attempt 2 duration: `500.094s`.
- Combined active Attempt duration: `684.384s` (`184.290s + 500.094s`).
