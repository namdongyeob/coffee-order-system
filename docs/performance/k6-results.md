# k6 결과

k6 실행 결과를 기록합니다. 수치는 실행 환경과 safe/heavy profile을 함께 적고 정합성 증거로 사용하지 않습니다.

## Issue #13 safe 기준선

환경: Windows 10 Pro 10.0.19045, AMD Ryzen 7 1800X, RAM 15.9GB, k6 v2.0.0, Docker 29.4.2, local profile, clean Compose MySQL·Redis·Kafka.

| 날짜 | profile | 스크립트 | 최대 VUs | duration | 주문 iterations | p95 | 주문 오류율 | HTTP RPS | threshold | 비고 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 2026-07-12 | safe 초기 Dev | `order-load.js` | 2 | 14s | 51 | 57.52ms | 0.00% | 3.63 | PASS | Content-Type 기반 분류, setup 포함 HTTP 53건 |
| 2026-07-12 | safe 초기 Dev | `order-stress.js` | 6 | 16s | 196 | 62.06ms | 0.00% | 12.38 | PASS | Content-Type 기반 분류, setup 포함 HTTP 202건 |
| 2026-07-12 | safe 초기 Dev | `order-spike.js` | 8 | 16s | 269 | 68.96ms | 0.00% | 16.88 | PASS | Content-Type 기반 분류, setup 포함 HTTP 277건 |
| 2026-07-12 | safe P1 재검증 | `order-load.js` | 2 | 14s | 47 | 179.33ms | 0.00% | 2.88 | PASS | JSON parse·필수 필드 분류, setup 포함 HTTP 49건 |
| 2026-07-12 | safe P1 재검증 | `order-stress.js` | 6 | 16s | 185 | 117.95ms | 0.00% | 11.76 | PASS | JSON parse·필수 필드 분류, setup 포함 HTTP 191건 |
| 2026-07-12 | safe P1 재검증 | `order-spike.js` | 8 | 16s | 271 | 69.35ms | 0.00% | 17.04 | PASS | JSON parse·필수 필드 분류, setup 포함 HTTP 279건 |

원문은 `docs/testing/evidence/issue-13/*-output.txt`, machine-readable summary는 `*-summary.json`에 있습니다. 이 짧은 safe 실행은 기준선 재현 확인이며 최대 처리량이나 병목 지점을 확정하지 않습니다.

## 후속 기록 템플릿

| 날짜 | profile | 스크립트 | 최대 VUs | duration | 주문 iterations | p95 | 주문 오류율 | HTTP RPS | threshold | 비고 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| YYYY-MM-DD | safe/heavy | script | 수치 | 시간 | 수치 | ms | % | req/s | PASS/FAIL | 실행 환경과 관찰 사항 |
