# Issue Attempt Log

Issue: #10
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/10
Branch: codex/issue-10-popular-menu-api

## Attempt 1 - Popular menu Top 3 API

### Generate

- `GET /api/menus/popular` Controller, response DTO, 최근 7일 인기 메뉴 조회 service와 Redis `ZUNION` 기반 ranking port 구현을 추가했습니다.
- 동점은 `orderCount` 내림차순 뒤 `menuId` 숫자 오름차순으로 애플리케이션에서 정렬합니다.
- DB에서 찾을 수 없는 Redis member는 제외하고 남은 메뉴로 Top 3를 채웁니다.

### Evaluate

- Level 2 Controller test와 Level 4 Redis Testcontainers integration test, fresh Level 1 전체 회귀는 Dev 보고 기준 PASS입니다.
- 초기 RED는 아직 구현되지 않은 `/api/menus/popular`이 404를 반환해 기대한 200과 불일치한 상태였습니다.

### Failure Cause

- TDD RED 시점에는 인기 메뉴 Top 3 API 구현이 없어 `404 Not Found`가 반환됐습니다.

### Change Scope

- 인기 메뉴 조회 API, Redis ranking 조회, 관련 Controller·Redis 통합 테스트, 인기 메뉴 정책 정본과 Issue #10 evidence만 변경합니다.
- Redis ranking write, Kafka Consumer, retry/DLT, replay/rebuild, DB 원천 집계와 QueryDSL 검증 조회는 변경하지 않습니다.

### Reverification

- Level 2 `MenuControllerTest`: PASS, 24초.
- Level 4 `PopularMenuRedisIntegrationTest`: 2 tests, failures 0, errors 0, PASS, 55.439초. 최근 7일 `ZUNION`, 동점 `2`/`10` 숫자 정렬, 범위 밖 날짜 제외, 삭제 메뉴 skip, 임시 key 미생성을 다룹니다.
- Level 1 `./gradlew.bat test --no-daemon`: PASS, 2분 02초.
- Baseline 전체 테스트는 Testcontainers ResourceReaper/Docker cleanup 대기로 종료가 지연됐고 최종 XML을 확인하지 못했습니다. PASS로 재구성하지 않으며 PARTIAL/미확인입니다.

### Next Attempt

- 독립 Review가 diff를 검토합니다. QA의 Level 5·6 결과와 HTTP 원문은 evidence에 반영했습니다.

## Independent QA - Level 5 and Level 6

### Evaluate

- PASS. `./gradlew.bat bootTestRun --no-daemon`으로 MySQL 8.4.5, Kafka 3.9.1, Redis 7.4.2와 애플리케이션을 기동했습니다.
- 애플리케이션은 43.966초에 시작했고 Kafka partition assigned, Redis `PING`의 `PONG`, Redis `DBSIZE`의 `0`을 확인했습니다.
- health와 `GET /api/menus/popular`은 모두 HTTP 200이었고 인기 메뉴 응답 JSON은 `[]`였습니다.

### Failure Cause

- 없음.

### Change Scope

- 코드 변경 없이 독립 runtime·HTTP 검증 결과만 evidence에 기록합니다.

### Reverification

- `curl.exe -sS -i http://localhost:8080/actuator/health` -> HTTP 200, `{"groups":["liveness","readiness"],"status":"UP"}`.
- `curl.exe -sS -i http://localhost:8080/api/menus/popular` -> HTTP 200, `Content-Type: application/json`, `[]`.
- Ctrl+C 뒤 Testcontainers ResourceReaper 지연은 20초 안에 정리됐고 기존 `rag-pgvector`만 남았습니다.

### Next Attempt

- 독립 Review와 GitHub Actions CI를 확인합니다. Level 6에서 populated Top 3 runtime 원문은 수집하지 않았으므로 API의 비어 있지 않은 순위 규칙은 Level 4 integration evidence 범위로만 주장합니다.

## CI evidence follow-up - PR #43 body field correction

### Generate

- PR #43의 initial quality-gates run `29168649292` 실패 원인을 확인했습니다. initial PR 본문은 harness가 요구하는 literal `Execution mode: STRICT`가 아니라 `STRICT mode:`를 사용했습니다.
- Dev가 live PR 본문을 `Execution mode: STRICT`와 비어 있지 않은 `Execution mode reason: ...`로 수정했습니다. 이 수정은 PR body에만 적용됐고 branch HEAD는 `c41cfee73735cb3188ff5db581a1c25d7ed0aace`로 변하지 않았습니다.

### Evaluate

- CI는 아직 PASS가 아닙니다. rerun은 기존 `pull_request` event payload를 유지해 수정 전 PR body를 검사했고 같은 형식 오류로 실패했습니다.

### Failure Cause

- initial CI event가 수집한 PR body는 `STRICT mode:` 형식이어서 harness의 exact field 검사와 불일치했습니다.
- live PR body 정정 뒤 동일 run을 rerun해도 stale event payload가 갱신되지 않습니다.

### Change Scope

- production/test/PR body를 수정하지 않고, 관찰된 CI 원인과 현재 PR body 상태를 Issue evidence에만 기록합니다.

### Reverification

- `gh pr view 43 --repo namdongyeob/coffee-order-system --json headRefOid,body,statusCheckRollup`에서 HEAD가 `c41cfee73735cb3188ff5db581a1c25d7ed0aace`이고 live PR body에 `Execution mode: STRICT`, `Execution mode reason:`이 있는 것을 확인했습니다.
- initial `quality-gates` run `29168649292`는 FAILURE이며 새 synchronize event의 CI 결과는 아직 없습니다.

### Next Attempt

- 이 evidence commit push가 새 `pull_request` synchronize event를 발생시킨 뒤, current PR body를 사용한 새 quality-gates CI 결과를 Main Coordinator가 확인합니다. CI PASS 전에는 완료 또는 merge를 주장하지 않습니다.

## Attempt 2 - Internal Review FAIL docs correction

### Generate

- 내부 Review가 P1과 P2 두 건으로 FAIL을 반환했습니다.
- P1은 Issue 전용 구현 계획서가 Context Router와 evidence 경로에서 도달할 수 없는 새 `docs/superpowers/plans/` tree에 놓인 문제입니다.
- P2는 PR #43에서 실제 재발한 exact PR body field와 stale event payload 실패가 반복 실수 정본에 반영되지 않은 문제입니다.

### Evaluate

- FAIL. production/test 동작 결함은 보고되지 않았지만 문서 위치와 재발 방지 evidence가 프로젝트 문서 규칙을 충족하지 못했습니다.
- HEAD `58bec6911fcd786967b8c54791950e23397186ef`의 quality-gates는 이 Attempt 시작 시 pending/in progress 상태입니다. PASS로 기록하지 않습니다.
- 독립 QA의 populated Top 3 E2E는 실행 중이며 결과를 아직 받지 않았으므로 이 Attempt에서 판정하거나 기록하지 않습니다.

### Failure Cause

- Issue 전용 계획을 기존 evidence 경로가 아니라 새 도달 불가능 tree에 생성했습니다.
- PR #43 재발은 CI evidence에만 기록하고 반복 실수 정본의 예방 규칙으로 연결하지 않았습니다.

### Change Scope

- 기존 계획서를 `docs/testing/evidence/issue-10/implementation-plan.md`로 history-preserving move합니다.
- `docs/ai/agent-mistakes.md`에 PR #43의 관찰 사실과 현재 예방 절차만 추가합니다. wrapper 또는 workflow 자동화는 별도 Issue로 남기고 구현하지 않습니다.
- Issue #10 attempt, commands, metrics에 Review FAIL과 Docs correction 상태만 반영합니다.

### Reverification

- `git diff --check` -> PASS.
- Issue #10 repository harness -> `Harness gate PASSED`.
- Harness unit -> 50 tests, PASS.
- production/test/Gradle/Redis/HTTP 검증은 Docs 역할 범위 밖이라 실행하지 않습니다.

### Next Attempt

- correction commit을 push한 뒤 내부 Review 재검토, 새 CI 결과, 독립 QA populated Top 3 raw evidence를 확인합니다. 세 결과가 확정되기 전에는 최종 PASS를 주장하지 않습니다.

## Independent QA populated Top 3 and final evidence update

### Generate

- production/test가 동일한 HEAD `58bec6911fcd786967b8c54791950e23397186ef`에서 실제 충전과 주문 두 건으로 Kafka Consumer와 Redis ranking을 채운 뒤 인기 메뉴 API를 호출했습니다.
- runtime 중 branch는 docs-only commit `a7d9477908a4ce7cb26a987224c70cf735ef7406`까지 전진했지만 production/test 변경은 없었습니다.

### Evaluate

- PASS. Redis prewrite 없이 주문 두 건이 MySQL `processed_event=2`와 Redis score `2`로 반영됐고 인기 메뉴 API가 rank 1 아메리카노, orderCount 2를 반환했습니다.
- 첫 charge curl의 HTTP 400 `INVALID_CHARGE_AMOUNT`는 PowerShell quoting 명령 오류입니다. corrected `Invoke-WebRequest`는 HTTP 200이므로 애플리케이션 결함으로 판정하지 않습니다.
- Docs correction HEAD `a7d9477908a4ce7cb26a987224c70cf735ef7406`의 quality-gates run `29169413405`는 SUCCESS로 새로 관찰했습니다.
- 내부 Review 재검토는 아직 없으므로 P1/P2가 해결됐다는 최종 Review PASS는 주장하지 않습니다.

### Failure Cause

- 애플리케이션 실패는 없습니다. 최초 charge command만 PowerShell JSON quoting이 잘못됐습니다.

### Change Scope

- Issue #10의 manual QA, HTTP raw evidence, commands, attempt, metrics, verification log만 갱신합니다.

### Reverification

- Focused Level 4: `BUILD SUCCESSFUL in 1m 26s`.
- Level 5: MySQL 8.4.5, Kafka 3.9.1, Redis 7.4.2, app 56.88초 start, Consumer partition assigned.
- Level 6: charge 200, orders 201/201, deterministic poll at 06:48:02.356에서 processed_event 2와 ZSCORE 2, health 200, popular 200과 raw `[{"rank":1,"menuId":1,"menuName":"아메리카노","orderCount":2}]`.
- Cleanup +5초와 +20초 확인에서 pre-existing `rag-pgvector`만 남았습니다.
- Final evidence docs: `git diff --check` PASS, Issue #10 harness PASS, harness unit 50 tests PASS.

### Next Attempt

- 이 evidence update를 push한 뒤 내부 Review 재검토와 새 synchronize quality-gates CI를 확인합니다. 둘 다 PASS하기 전에는 merge 준비 완료를 주장하지 않습니다.
