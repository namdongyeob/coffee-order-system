# k6 결과

k6 실행 결과를 기록합니다. 수치는 실행 환경과 safe/heavy profile을 함께 적고 정합성 증거로 사용하지 않습니다.

## Issue #13 safe 기준선

환경: Windows 10 Pro 10.0.19045, AMD Ryzen 7 1800X, RAM 15.9GB, k6 v2.0.0, Docker 29.4.2, local profile, clean Compose MySQL·Redis·Kafka.

| 날짜 | profile | 스크립트 | 최대 VUs | duration | 주문 iterations | p95 | 주문 오류율 | HTTP RPS | threshold | 비고 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 2026-07-12 | safe | `order-load.js` | 2 | 14s | 51 | 57.52ms | 0.00% | 3.63 | PASS | setup 충전 2건 포함 HTTP 53건 |
| 2026-07-12 | safe | `order-stress.js` | 6 | 16s | 196 | 62.06ms | 0.00% | 12.38 | PASS | setup 충전 6건 포함 HTTP 202건 |
| 2026-07-12 | safe | `order-spike.js` | 8 | 16s | 269 | 68.96ms | 0.00% | 16.88 | PASS | setup 충전 8건 포함 HTTP 277건 |

원문은 `docs/testing/evidence/issue-13/*-output.txt`, machine-readable summary는 `*-summary.json`에 있습니다. 이 짧은 safe 실행은 기준선 재현 확인이며 최대 처리량이나 병목 지점을 확정하지 않습니다.

## 후속 기록 템플릿

| 날짜 | profile | 스크립트 | 최대 VUs | duration | 주문 iterations | p95 | 주문 오류율 | HTTP RPS | threshold | 비고 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| YYYY-MM-DD | safe/heavy | script | 수치 | 시간 | 수치 | ms | % | req/s | PASS/FAIL | 실행 환경과 관찰 사항 |
