# Issue #14 Attempt Log

Issue: #14
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/14
Branch: codex/issue-14-ranking-rebuild

## Attempt 1

### Generate

- Generate start: 2026-07-13T10:30:00+09:00. 첫 RED 명령 이전에 확인 가능한 분 단위 시작 범위입니다.
- `RankingRebuildServiceIntegrationTest` RED 뒤 maintenance guard, active group 검사, Redis lock, earliest replay와 end boundary, DB 비교, temp/backup Lua swap, 정상 group offset 이동을 구현했습니다.
- live Issue의 `[snapshot-7d,snapshot)`가 교차하는 distinct date key 전체를 동일 temp/live set으로 교체합니다.

### Evaluate

- PASS. focused 6건, related ranking suite, 전체 57건, Level 5 성공과 DB mismatch fail-closed가 통과했습니다.
- 중간 RED는 service 부재 compile 실패였습니다. ObjectMapper bean 부재와 기존 Redis slice의 Kafka placeholder 로딩 실패도 실제 출력으로 확인했습니다.

### Failure Cause

- 초기 RED는 production service가 아직 없어서 발생했습니다.
- 첫 GREEN context 실패는 ObjectMapper bean 부재였고, related context 실패는 일반 slice에도 rebuild service가 로드돼 Kafka placeholder를 요구했기 때문입니다.
- 기존 DLT test의 단발 timing 실패는 focused fresh rerun과 마지막 full에서 재현되지 않았습니다.

### Change Scope

- ranking rebuild service·runner·consumer startup flag, 관련 unit/integration test, recovery ADR·strategy·runbook과 Issue #14 evidence만 변경했습니다.

### Reverification

- Reverification end: 2026-07-13T11:28:02+09:00.
- focused 6건 PASS, related ranking suite PASS, 전체 57건 failures/errors 0, Level 5 success/mismatch/cleanup PASS입니다.

### Next Attempt

- Fresh Review의 P1 remediation.

## Attempt 2

### Generate

- Fresh Review의 offset 부분 성공·timeout 보상과 lock lease 만료 P1 두 건을 현재 head와 대조했습니다.
- pre-swap offset snapshot, 보상·broker 재조회, 위험 단계 lock renewal을 추가했습니다.

### Evaluate

- PASS. 실제 2-partition Kafka에서 부분 offset 변경 뒤 timeout, 정상 offset 복원, Redis rollback을 검증했습니다.
- offset 보상 자체 실패는 완전 rollback을 주장하지 않는 명시적 fail-closed exception으로 검증했습니다.

### Failure Cause

- 기존 구현은 Redis만 rollback했고 offset의 부분 성공·불확실 completion을 보상하지 않았습니다.
- lock은 token unlock만 있었고 긴 replay 뒤 위험 변경 직전 lease 소유권을 재확인하지 않았습니다.

### Change Scope

- Issue #14 ranking rebuild service, 최소 lock·offset component, 관련 test와 Issue #14 evidence만 변경했습니다.

### Reverification

- Focused 10 tests PASS, `BUILD SUCCESSFUL in 1m 29s`.
- Related ranking suite PASS, `BUILD SUCCESSFUL in 3m 43s`.
- 전체 61 tests, failures/errors 0, Level 5 local maintenance success와 cleanup이 PASS했습니다.

### Role verification history

- 첫 Fresh Review는 offset 부분 성공·timeout 보상 누락과 lock lease 만료의 P1 두 건, 비자정 snapshot 8개 날짜 경계 테스트 누락의 P2 한 건을 보고하고 `REVISE`로 판정했습니다.
- 같은 Dev가 허용된 한 번의 remediation에서 두 P1을 수정하고 P2 경계 테스트를 함께 추가했습니다.
- remediation 뒤 fresh read-only Review는 신규 P0/P1/P2 없이 승인된 정책과 보상·lock·날짜 경계 계약을 충족한다고 `APPROVED`로 판정했습니다.
- independent QA는 focused Level 4 10건을 1분 41초에 PASS했고, Level 5 성공 rebuild·DB mismatch fail-closed·cleanup을 독립 재검증해 `PASS`로 판정했습니다.

### Next Attempt

- 없음.

## Attempt 3

### Generate

- 두 번째 fresh Review가 current earliest offset이 `0`보다 크다는 이유만으로 rebuild를 거부하면 최근 7일 데이터가 보존된 정상 retention 상황도 실행할 수 없다는 P1을 보고했습니다.
- 사용자 승인 범위에서 offset 0 전제를 제거하고 current earliest replay와 DB exact comparison을 완전성 gate로 유지했습니다.
- current earliest `> 0` recent 보존 성공과 필요한 recent event 실제 유실 실패를 actual Kafka `deleteRecords`로 검증하는 통합 테스트를 추가했습니다.

### Evaluate

- RED는 이전 offset 0 전제에서 예상대로 실패했고, GREEN 2건과 최초 focused 11건·related 회귀는 PASS했습니다.
- 최초 full과 깨끗한 격리 실행에서 기존 DLT timing failure가 반복돼 retention commit 없이 안전 정지했습니다.
- 원인은 범위 밖 blocker Issue #77로 분리됐고, #77 merge 뒤 일반 merge commit으로 최신 main을 반영했습니다. 공통 append-only `verification-log.md` 단일 충돌은 양쪽 행을 모두 보존해 해결했습니다.

### Failure Cause

- Issue #14 결함은 current earliest가 0보다 큰 정상 retention 상태를 곧바로 실패 처리한 잘못된 전제였습니다.
- DLT failure는 retention diff와 무관한 기존 timing 결함이었고 Issue #77에서 listener assignment 동기화로 해결됐습니다. 이 PR은 DLT production/test를 수정하지 않았습니다.

### Change Scope

- `RankingRebuildService`의 offset 0 거부 제거, `RankingRebuildServiceIntegrationTest`의 retained/lost retention 계약, Issue #14 evidence만 변경했습니다.
- 최신 main merge에서는 #77 DLT test/evidence와 양쪽 verification-log 행만 반영했습니다.

### Reverification

- 실행 head `e58a90d544f5b86cdfe19af3550d9e0041d0a46e`에서 `.\gradlew.bat cleanTest test --tests "*RankingRebuildServiceTest" --tests "*RankingRebuildServiceIntegrationTest" --no-daemon --max-workers=1`을 실행해 focused 11 tests PASS, `BUILD SUCCESSFUL in 2m 34s`를 확인했습니다.
- 같은 head에서 `.\gradlew.bat cleanTest test --tests "*Ranking*" --tests "*PopularMenu*" --no-daemon --max-workers=1`을 실행해 related ranking/Kafka 28 tests PASS, `BUILD SUCCESSFUL in 4m 57s`를 확인했습니다.
- 같은 head에서 `.\gradlew.bat cleanTest test --no-daemon --max-workers=1`을 실행해 전체 62 tests PASS, `BUILD SUCCESSFUL in 4m 53s`를 확인했습니다.
- Level 5에서 earliest `1` recent 보존 success와 earliest/latest `2/2` actual recent loss의 DB mismatch fail-closed, live·normal offset 보존, temp/backup cleanup을 확인했습니다.

### Role verification history

- 두 번째 Review P1 한 건을 사용자가 마지막 제한 remediation으로 승인했습니다.
- 반복 DLT failure는 Issue #77 blocker로 분리했고 #77 merge·close 뒤 같은 PR에서 검증을 재개했습니다.
- Fresh read-only Review는 P0/P1 없이 `APPROVED`로 판정했습니다. 비차단 P2 세 건은 PR metadata 최소화, `commands.md`의 최종 명령·실행 head 명시, malformed payload·완전 empty topic/DB의 독립 테스트명 보강 권고였습니다.
- Independent QA의 첫 focused 실행은 IntelliJ Gradle daemon 병행으로 결과를 폐기했고, 두 번째 실행은 사용자가 중복 Testcontainers 비용 경량화를 승인해 중단했습니다. 두 실행은 PASS 근거와 테스트 수에 포함하지 않았습니다.
- Independent QA는 실행 head의 Dev 명령·XML, Level 5 원문, 테스트 목록과 cleanup receipt를 읽기 전용으로 대조해 evidence audit `PASS`를 보고했습니다. 이 QA는 Level 4·5를 독립 완주하지 않았습니다.
- QA가 시작한 리소스 cleanup 직후 Java/Gradle 프로세스 0개, Docker container 0개, port 8080 free를 확인했습니다. 이후 외부에서 시작된 `docker-*` container 5개는 QA 소유가 아니므로 건드리지 않았습니다.

### Next Attempt

- Docs final sync 뒤 fresh final Review.
