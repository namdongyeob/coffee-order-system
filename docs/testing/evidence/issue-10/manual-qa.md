# Issue #10 Manual QA

## Dev 관찰

- Level 2 Controller 계약은 `MenuControllerTest`로 PASS했습니다.
- Level 4 Redis Testcontainers integration은 최근 7일 합산, 동점 `2`/`10` 숫자 정렬, 범위 밖 날짜 제외, 삭제 메뉴 skip, 임시 key 미생성을 PASS했습니다.

## Pending 독립 QA

- Level 5 required: 실제 애플리케이션과 Redis 연결, health endpoint를 확인해야 합니다.
- Level 6 required: 실제 `GET /api/menus/popular` 요청과 응답 JSON 원문을 이 파일과 `http/issue-10-popular-menu.http`에 기록해야 합니다.
- 현재는 실행 가능한 HTTP 요청 템플릿만 있으며 실제 응답을 아직 기록하지 않았습니다.
