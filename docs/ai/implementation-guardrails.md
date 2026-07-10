# 구현 가드레일

- Generic Manager보다 명시적인 서비스 메서드를 우선합니다.
- 패키지는 도메인 책임이 드러나도록 구성합니다.
- 트랜잭션은 서비스 계층에서 관리합니다.
- Repository 락 메서드는 필요한 위치에만 둡니다.
- Redis 랭킹을 원천 데이터로 취급하지 않습니다.
- MVP에서는 DLT 재처리를 공개 API로 만들지 않습니다.
- Testcontainers 검증과 docker-compose 수동 검증을 검증 로그에서 구분합니다.

## Dev Agent 범위 제한

- 하나의 Dev Agent는 하나의 Issue만 구현합니다.
- 같은 PR에서 문서 정리, API 구현, 인프라 구성, 성능 테스트를 한꺼번에 처리하지 않습니다.
- 공통 예외 포맷, DB 스키마, Kafka 설정처럼 여러 기능에 영향을 주는 변경은 별도 chore 또는 architecture Issue로 분리합니다.
- Redis rebuild runner, DLT replay script, k6 테스트는 MVP API 구현과 분리합니다.

## Review Agent 기준

- 요구사항 누락을 먼저 봅니다.
- 테스트 누락을 봅니다.
- 포인트 정합성, Kafka 중복 처리, Redis 랭킹 중복 증가 가능성을 봅니다.
- 과한 추상화와 Issue 범위 초과를 봅니다.

## QA Agent 기준

- Mock 테스트만 있는 경우 실제 검증 미완료로 표시합니다.
- Kafka/Redis 통합 검증이 없으면 Level 4 미완료로 표시합니다.
- 로컬 서버 기동과 실제 API 호출이 없으면 Level 5, Level 6 미완료로 표시합니다.
- Dev 결과와 독립적으로 필요한 focused test, 전체 smoke test, 실제 환경 검증을 실행합니다.
- 검증 누락은 직접 수정하지 않고 follow-up Issue 후보로 남깁니다.
