# ADR-007 k6 테스트 우선순위

## 상태

Accepted.

## 결정

k6는 Load, Stress, Spike 순서로 사용합니다. k6 결과를 정합성 검증 근거로 사용하지 않습니다.

## 이유

기준선을 먼저 잡아야 Stress와 Spike 결과를 해석할 수 있습니다. 포인트 정합성은 JUnit 동시성 테스트로 검증합니다.