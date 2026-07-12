# Issue #13 Manual QA

## 실행 환경

- Windows 10 Pro 10.0.19045.
- AMD Ryzen 7 1800X, RAM 15.9GB.
- k6 v2.0.0, Docker client/server 29.4.2.
- clean project Compose의 MySQL 8.4.5, Redis 7.4.2, Kafka 3.9.1.
- local profile 앱은 14.764초에 시작했고 Flyway migration 4개와 Redis·Kafka 연결을 확인했습니다.

## 관찰 결과

| 시나리오 | 최대 VU | 주문 iterations | p95 | 주문 오류율 | HTTP RPS | 판정 |
| --- | --- | --- | --- | --- | --- | --- |
| Load safe | 2 | 51 | 57.52ms | 0.00% | 3.63 | PASS |
| Stress safe | 6 | 196 | 62.06ms | 0.00% | 12.38 | PASS |
| Spike safe | 8 | 269 | 68.96ms | 0.00% | 16.88 | PASS |

각 VU는 `130000 + __VU` synthetic user를 사용했고 setup 충전은 모두 HTTP 200, 주문은 모두 HTTP 201·JSON이었습니다. threshold는 `checks`, `http_req_failed`, p95, `order_success_rate`, `order_error_rate` 모두 통과했습니다.

## Adversarial QA와 한계

- 알 수 없는 profile은 fail-closed하도록 구현했고 contract test는 safe 기본 최대 VU, 필수 threshold와 세 profile 이름을 검사합니다.
- heavy profile은 실제 실행하지 않았습니다. safe 실행은 최대 처리량·병목·운영 SLO를 확정하지 않습니다.
- Kafka lag와 Redis latency는 script가 직접 수집하지 않았습니다.
- k6 결과는 포인트·주문 정합성 증명이 아닙니다.

## Cleanup receipt

- 앱을 Ctrl+C로 종료했습니다.
- 프로젝트 Compose를 `down -v`로 정리했습니다.
- `docker compose ... ps`는 empty였고 health endpoint는 stopped였습니다.
- 기존 MySQL 3306과 다른 프로젝트 컨테이너는 조작하지 않았습니다.
