# Issue #13 Commands

## 정적·contract 검증

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `k6 version` | 실행 도구 확인 | PASS, k6 v2.0.0 windows/amd64 |
| `python -m unittest k6.tests.test_scenarios` | script 구조와 inspect 계약 | RED 4 errors 뒤 GREEN 2 tests PASS |
| `k6 inspect k6/order-load.js` | Load options·threshold parse | PASS, safe 최대 2 VU |
| `k6 inspect k6/order-stress.js` | Stress options·threshold parse | PASS, safe 최대 6 VU |
| `k6 inspect k6/order-spike.js` | Spike options·threshold parse | PASS, safe 최대 8 VU |

## Level 5~7 실제 실행

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `docker compose -f docker/compose.yaml down -v` | clean project environment | PASS |
| `docker compose -f docker/compose.yaml up -d` | MySQL·Redis·Kafka 기동 | PASS, 세 service healthy |
| `$env:SPRING_PROFILES_ACTIVE='local'; .\gradlew.bat bootRun --no-daemon` | local profile 앱 기동 | PASS, 14.764초 |
| `k6 run --no-color --summary-export docs/testing/evidence/issue-13/load-summary.json k6/order-load.js` | safe Load | PASS, 51 주문 iterations, p95 57.52ms, 오류율 0% |
| `k6 run --no-color --summary-export docs/testing/evidence/issue-13/stress-summary.json k6/order-stress.js` | safe Stress | PASS, 196 주문 iterations, p95 62.06ms, 오류율 0% |
| `k6 run --no-color --summary-export docs/testing/evidence/issue-13/spike-summary.json k6/order-spike.js` | safe Spike | PASS, 269 주문 iterations, p95 68.96ms, 오류율 0% |
| 앱 Ctrl+C, `docker compose -f docker/compose.yaml down -v`, `docker compose -f docker/compose.yaml ps` | cleanup | PASS, compose 목록 empty·health endpoint stopped |

## Repository 검증

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `python scripts/harness_gate.py --issue 13 --branch codex/issue-13-k6-scenarios --base-ref origin/main --check-links` | Issue repository gate | PASS |
| `git diff --check` | whitespace·patch 검사 | PASS |
| `python scripts/harness_gate.py --issue 13 --pr-body-file <저장소 밖 UTF-8 no-BOM 파일>` | 한국어 PR body preflight | PASS |

## 원문

- Load: `load-output.txt`, `load-summary.json`.
- Stress: `stress-output.txt`, `stress-summary.json`.
- Spike: `spike-output.txt`, `spike-summary.json`.
