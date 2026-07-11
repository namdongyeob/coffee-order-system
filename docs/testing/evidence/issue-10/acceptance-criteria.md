# Issue #10 Acceptance Criteria

- [x] `GET /api/menus/popular`은 `rank`, `menuId`, `menuName`, `orderCount`를 반환합니다.
- [x] 오늘을 포함한 최근 7일의 Redis 일별 ZSET score를 합산합니다.
- [x] Redis 7.4의 저장 없는 `ZUNION`을 사용하며, `ZUNIONSTORE` 임시 key, TTL, 삭제 방식은 사용하지 않습니다.
- [x] 동점은 Redis 문자열 사전순이 아니라 애플리케이션의 `orderCount` 내림차순, `menuId` 숫자 오름차순으로 정렬합니다. `2`와 `10` 동점 회귀 테스트를 포함합니다.
- [x] DB에 없는 삭제 메뉴 member를 건너뛰고 남은 순위로 최대 Top 3를 채웁니다.
- [x] `docs/domain/popular-menu-policy.md`에 동점 정렬과 삭제 메뉴 skip 규칙을 정본으로 기록합니다.
- [ ] 독립 Review, QA, GitHub Actions CI 결과를 반영합니다.
- [x] 독립 QA가 Level 5 runtime과 Level 6 실제 HTTP 원문을 확인했습니다.

Execution mode: STRICT
Execution mode reason: Redis 인프라 조회와 HTTP API, 도메인 정책 정본 및 실제 런타임 검증을 함께 변경하므로 Dev, 별도 Review, QA, Docs, CI가 필요합니다.
Level 5 required: YES
Level 5 reason: 실제 애플리케이션의 Redis 연결과 API runtime 기동을 확인해야 합니다.
Level 6 required: YES
Level 6 reason: 새 공개 HTTP API의 실제 요청과 응답 JSON 원문을 evidence로 남겨야 합니다.

## 완료 경계

- Dev 보고 기준으로 Level 2, Level 4, Level 1은 PASS입니다.
- Level 5와 Level 6은 required이며 독립 QA가 PASS했습니다. Level 6 runtime Redis는 비어 있어 `[]` 응답만 관찰했고, populated Top 3 HTTP 원문은 관찰하지 않았습니다. populated 순위·동점·삭제 메뉴 동작은 Level 4 Redis Testcontainers integration으로 검증한 별도 evidence입니다.
- `ZUNION`은 `LIMIT`을 제공하지 않으므로 결과 전체를 받은 뒤 숫자 정렬, 삭제 메뉴 제외, Top 3 절단을 적용합니다. 대규모 cardinality 최적화는 Issue #10 범위 밖입니다.
