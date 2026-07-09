# ADR-001 3계층 아키텍처

## 상태

Accepted.

## 결정

기본 아키텍처는 Controller-Service-Repository 3계층으로 구성합니다.

## 이유

이번 과제는 API, 정합성, 동시성 설명이 핵심입니다. 단순한 3계층 구조가 리뷰와 검증에 유리하고 불필요한 추상화를 줄입니다.

## 세부 정책

세부 책임과 금지 규칙은 `docs/architecture/layered-design-policy.md`를 따릅니다.
