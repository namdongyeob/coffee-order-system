# k6 계획

## Load Test

- `order-load.js`로 VU를 점진적으로 증가시킵니다.
- p95, 실패율, RPS, Kafka lag, Redis latency를 관찰합니다.
- safe 기본값은 0 → 1 → 2 → 0 VU이며 기준선 확인용입니다.

## Stress Test

- `order-stress.js`로 부하를 단계적으로 증가시킵니다.
- 테스트 PC 사양과 환경을 함께 기록합니다.
- safe 기본값은 최대 6 VU까지만 관찰하고, 실제 저하 구간 탐색은 `K6_PROFILE=heavy`를 명시한 격리 환경에서만 수행합니다.

## Spike Test

- `order-spike.js`로 점심시간 급상승 트래픽을 가정합니다.
- 락 실패 응답과 API 안정성을 확인합니다.
- safe 기본값은 1 VU에서 8 VU로 짧게 상승한 뒤 다시 회복합니다.

## 공통 계약

- clean Compose와 local profile 앱을 전제로 하며 `setup()`이 VU별 synthetic 사용자 포인트를 준비합니다.
- 주문 HTTP 201과 JSON 응답만 성공으로 분류하고 나머지는 오류율에 반영합니다.
- safe profile은 로컬 기본값이고 heavy profile은 명시적으로 선택해야 합니다.
- p95, HTTP 실패율, 주문 성공·오류율, RPS, iteration 수와 VU를 summary에 남깁니다.
- Kafka lag와 Redis latency는 현재 스크립트가 직접 수집하지 않으므로 별도 관찰 항목으로 남습니다.

## 정합성 테스트가 아님

k6는 포인트 정합성을 증명하지 않습니다. 정합성은 JUnit 동시성 테스트로 검증합니다.
