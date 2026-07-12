# Issue #45 Acceptance Criteria

Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/45

Execution mode: STRICT
Execution mode reason: QA와 CI의 전체 회귀 판정 책임을 바꾸는 workflow policy change입니다.
Level 5 required: NO
Level 5 reason: 애플리케이션 런타임이나 인프라 실행 경로를 변경하지 않습니다.
Level 6 required: NO
Level 6 reason: HTTP 계약이나 실제 API 실행 경로를 변경하지 않습니다.

- [x] QA의 로컬 Level 1 전체 회귀 의무를 제거하고 QA의 focused 및 Level 3~6 독립 검증은 유지합니다.
- [x] GitHub Actions `quality-gates`를 전체 Level 1 회귀의 최종·단독 독립 gate로 기록합니다.
- [x] CI가 unavailable, pending 또는 FAIL이면 QA PASS가 대체하지 못하고 PR이 blocked임을 기록합니다.
- [x] 제거한 QA 전체 회귀의 대체 층과 #7, #9, #40 비교 기준선을 기록합니다.
- [ ] Fresh Review, independent QA, Docs final synchronization, and CI remain coordinator gates.
