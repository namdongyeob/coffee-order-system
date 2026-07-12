# Issue #13 Commands

## 정적·contract 검증

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `k6 version` | 실행 도구 확인 | PASS, k6 v2.0.0 windows/amd64 |
| `python -m unittest k6.tests.test_scenarios` | script 구조·inspect·malformed JSON 분류 계약 | 초기 RED 4 errors/2 tests GREEN, P1 RED 1 failure 뒤 최종 3 tests PASS |
| `k6 inspect k6/order-load.js` | Load options·threshold parse | PASS, safe 최대 2 VU |
| `k6 inspect k6/order-stress.js` | Stress options·threshold parse | PASS, safe 최대 6 VU |
| `k6 inspect k6/order-spike.js` | Spike options·threshold parse | PASS, safe 최대 8 VU |

## Level 5~7 실제 실행

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `docker compose -f docker/compose.yaml down -v` | clean project environment | PASS |
| `docker compose -f docker/compose.yaml up -d` | MySQL·Redis·Kafka 기동 | PASS, 세 service healthy |
| `$env:SPRING_PROFILES_ACTIVE='local'; .\gradlew.bat bootRun --no-daemon` | local profile 앱 기동 | PASS, 14.764초 |
| `k6 run --no-color --summary-export docs/testing/evidence/issue-13/load-summary.json k6/order-load.js` | P1 변경 후 safe Load | PASS, 47 주문 iterations, p95 179.33ms, 오류율 0% |
| `k6 run --no-color --summary-export docs/testing/evidence/issue-13/stress-summary.json k6/order-stress.js` | P1 변경 후 safe Stress | PASS, 185 주문 iterations, p95 117.95ms, 오류율 0% |
| `k6 run --no-color --summary-export docs/testing/evidence/issue-13/spike-summary.json k6/order-spike.js` | P1 변경 후 safe Spike | PASS, 271 주문 iterations, p95 69.35ms, 오류율 0% |
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

초기 Dev 실행은 51·196·269 주문 iterations와 p95 57.52·62.06·68.96ms였습니다. P1 변경 뒤 위 원문 파일을 current-code 재검증 결과로 교체했으며 두 실행의 차이는 `attempt-log.md`와 성능 결과 문서에 구분했습니다.
