# Dev Agent 구현 가드레일

이 문서는 Dev Agent의 구현 기준입니다. 역할, 쓰기 권한, 실행 모드는 [오케스트레이션 정책](orchestration-policy.md), 테스트 Level과 실행 소유권은 [테스트 전략](../testing/test-strategy.md), Review와 QA의 판정은 각각 [Review Gate](review-gate.md), [QA Gate](qa-gate.md)를 따릅니다.

- Generic Manager보다 명시적인 서비스 메서드를 우선합니다.
- 패키지는 도메인 책임이 드러나도록 구성합니다.
- 트랜잭션은 서비스 계층에서 관리합니다.
- Repository 락 메서드는 필요한 위치에만 둡니다.
- Redis 랭킹을 원천 데이터로 취급하지 않습니다.
- MVP에서는 DLT 재처리를 공개 API로 만들지 않습니다.
- Testcontainers 검증과 docker-compose 수동 검증을 검증 로그에서 구분합니다.

## 범위 제한

- 하나의 Dev Agent는 하나의 Issue만 구현합니다.
- 같은 PR에서 문서 정리, API 구현, 인프라 구성, 성능 테스트를 한꺼번에 처리하지 않습니다.
- 공통 예외 포맷, DB 스키마, Kafka 설정처럼 여러 기능에 영향을 주는 변경은 별도 chore 또는 architecture Issue로 분리합니다.
- Redis rebuild runner, DLT replay script, k6 테스트는 MVP API 구현과 분리합니다.
