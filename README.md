# Coffee Order System

Spring Boot 기반 커피 주문 시스템 개인과제입니다.

## 목표

커피 메뉴 조회, 포인트 충전, 주문/결제, 최근 7일 인기 메뉴 조회를 구현하고, 동시성, 데이터 정합성, Kafka 이벤트 처리, Redis 랭킹, 테스트 전략을 설명하고 검증합니다.

## 필수 API

- 커피 메뉴 목록 조회.
- 포인트 충전.
- 커피 주문/결제.
- 최근 7일 인기 메뉴 Top 3 조회.

## 주요 결정

- 기본 구조는 Controller-Service-Repository 3계층으로 시작합니다.
- 주문 생성과 포인트 차감은 하나의 DB 트랜잭션으로 처리합니다.
- 포인트 정합성은 DB 비관적 락으로 최종 보장합니다.
- Redisson은 `userId` 기준 주문/결제 진입 제어에 사용합니다.
- `OrderCompletedEvent`를 Kafka로 발행합니다.
- Redis ZSET은 실시간 인기 메뉴 랭킹용 파생 데이터로 사용합니다.
- 재시도 후 실패한 Kafka 메시지는 DLT로 이동합니다.
- MVP에서는 DLT 재처리 API를 만들지 않고 수동 또는 스크립트 방식으로 다룹니다.
- Redis 랭킹 유실 시 Kafka replay 기반 복구는 후반 도전 Issue로 분리합니다.

## 문서 진입점

- 요구사항: `docs/product/requirements.md`.
- 작업 순서와 이슈 초안: `docs/product/github-issues.md`.
- 도메인 규칙: `docs/domain/domain-rules.md`.
- 아키텍처: `docs/architecture/overview.md`.
- 강의 개념 매핑: `docs/architecture/lecture-mapping.md`.
- API 명세: `docs/api/api-spec.md`.
- DB/ERD: `docs/db/erd.md`.
- 테스트 전략: `docs/testing/test-strategy.md`.
- 의존성 점검: `docs/onboarding/dependency-check.md`.
- AI 작업 규칙: `docs/ai/agent-rules.md`.

## 추천 작업 순서

1. `docs/onboarding/dependency-check.md`를 보고 현재 의존성 보강 여부를 확정합니다.
2. `docs/product/github-issues.md`의 순서대로 GitHub Issue를 만듭니다.
3. 한 번에 하나의 Issue만 구현합니다.
4. 구현 중 API, DB, 복구 정책이 바뀌면 연결 문서를 함께 수정합니다.
5. 완료 전 `docs/testing/verification-log.md`에 실제 검증 결과를 남깁니다.

## 검증 규칙

완료 주장은 반드시 검증 레벨과 실제 명령 결과를 `docs/testing/verification-log.md`에 남긴 뒤에만 합니다.
