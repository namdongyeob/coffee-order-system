# Issue #13 Commands

## 정적·contract 검증

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `k6 version` | 실행 도구 확인 | PASS, k6 v2.0.0 windows/amd64 |
| `python -m unittest k6.tests.test_scenarios` | script 구조·inspect·actual `createOrder` 응답·Rate 계약 | 초기 RED 4 errors/2 tests GREEN, 첫 P1 RED 1 failure, 별도 제한 P1 RED 1 failure 뒤 최종 3 tests PASS |
| `k6 inspect k6/order-load.js` | Load options·threshold parse | PASS, safe 최대 2 VU |
| `k6 inspect k6/order-stress.js` | Stress options·threshold parse | PASS, safe 최대 6 VU |
| `k6 inspect k6/order-spike.js` | Spike options·threshold parse | PASS, safe 최대 8 VU |
| `k6 run --no-color k6/tests/order-response-contract.js` | 마지막 test-only missing-field actual Rate 연결 | PASS, contract assertions 7/7; invalid 응답 내부 checks 5건은 기대 실패 |

## Level 5~7 실제 실행

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `docker compose -f docker/compose.yaml down -v` | clean project environment | PASS |
| `docker compose -f docker/compose.yaml up -d` | MySQL·Redis·Kafka 기동 | PASS, 세 service healthy |
| `$env:SPRING_PROFILES_ACTIVE='local'; .\gradlew.bat bootRun --no-daemon` | local profile 앱 기동 | PASS, 14.764초 |
| `k6 run --no-color --summary-export docs/testing/evidence/issue-13/load-summary.json k6/order-load.js` | actual Rate 연결 후 safe Load | PASS, 41 주문 iterations, p95 293.96ms, 오류율 0% |
| `k6 run --no-color --summary-export docs/testing/evidence/issue-13/stress-summary.json k6/order-stress.js` | actual Rate 연결 후 safe Stress | PASS, 111 주문 iterations, p95 628.41ms, 오류율 0% |
| `k6 run --no-color --summary-export docs/testing/evidence/issue-13/spike-summary.json k6/order-spike.js` | actual Rate 연결 후 safe Spike | PASS, 237 주문 iterations, p95 145.15ms, 오류율 0% |
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

초기 Dev 실행은 51·196·269 주문 iterations와 p95 57.52·62.06·68.96ms, 첫 P1 재검증은 47·185·271건과 p95 179.33·117.95·69.35ms였습니다. 위 원문 파일은 별도 제한 P1의 current-code 결과이며 세 실행은 `attempt-log.md`와 성능 결과 문서에 구분했습니다.

마지막 test-only remediation은 runtime 경로를 변경하지 않아 Level 7을 반복하지 않았습니다. 직전 current-head Level 7 결과와 cleanup evidence를 승계하고 actual contract만 재실행했습니다.

## 최종 승인 검증 요약

| 검증 | 최종 결과 |
| --- | --- |
| `k6 run --no-color k6/tests/order-response-contract.js` | PASS, exit 0, contract assertions 7/7. invalid-response 내부 check 실패 5건은 의도된 거부 관찰 |
| `python -m unittest k6.tests.test_scenarios` | PASS, 3/3 |
| 세 `k6 inspect` | PASS |
| Issue #13 repository gate, `git diff --check`, live 한국어 PR body preflight | PASS |
| 마지막 test-only 변경 뒤 Level 7 | 미실행. runtime/helper 불변이므로 직전 current-code Level 7 및 cleanup evidence 유효 |
