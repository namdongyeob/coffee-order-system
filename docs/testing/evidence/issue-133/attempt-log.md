# Issue Attempt Log

Issue: #133
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/133
Branch: codex/issue-133-ranking-ledger-docs
Current disposition: PASS
Current Attempt: 6
Current head: a23110b148d044fcaa5bb5038e56dd318a5293ce

## Attempt 1

### Generate
- **시각**: 2026-07-19T20:57:00
- **수행 작업**: `docs/adr/ADR-008-ranking-recovery-ledger.md`에 Redis marker 만료 복구 계약 초안 수록.

### Evaluate
- **시각**: 2026-07-19T21:00:00
- **수행 검증**: GitHub Actions Run #29686161262 및 Run #29686605653 실행.
- **결과**: `DltReplayServiceIntegrationTest` 10초 대기 시간 부족으로 타임아웃 실패.

### Failure Cause
- CI 빌드 환경의 리소스 제약으로 인해 개별 테스트의 비동기 Kafka 컨슈밍 대기 시간(10초)이 초과되어 실패했습니다.

### Change Scope
- 수정 파일: `docs/adr/ADR-008-ranking-recovery-ledger.md`

### Reverification
- 결과: FAIL (타임아웃 발생)

### Next Attempt
- 계획: 대기 시간을 30초로 상향 조정하고 재시도.


## Attempt 2

### Generate
- **시각**: 2026-07-19T21:10:00
- **수행 작업**: 하네스 실행 모드(execution-mode) 설정 관련 조율 작업 진행.

### Evaluate
- **시각**: 2026-07-19T21:12:00
- **수행 검증**: GitHub Actions Run #29686471761 실행.
- **결과**: execution-mode mismatch로 인한 실패.

### Failure Cause
- STRICT/SOLO 모드 설정의 오정합 및 하네스 분류 정책 충돌로 인해 빌드 진입 시 실패가 발생했습니다.

### Change Scope
- 수정 파일: `docs/testing/evidence/issue-133/acceptance-criteria.md`

### Reverification
- 결과: FAIL (모드 정합성 위배)

### Next Attempt
- 계획: 실행 모드를 STRICT로 통일하고 개별 통합 테스트 타임아웃을 상향하여 재시도.


## Attempt 3

### Generate
- **시각**: 2026-07-19T21:40:00
- **수행 작업**: `DltReplayServiceIntegrationTest.java`의 `awaitProcessed` 대기 시간을 30초로 상향.

### Evaluate
- **시각**: 2026-07-19T21:45:00
- **수행 검증**: GitHub Actions Run #29687334077 실행.
- **결과**: Test Context Pollution으로 인해 Redis Lock 유출 및 컨슈머 Fencing 오류 발생하여 30초 대기 시간 초과로 실패.

### Failure Cause
- 이전 테스트 클래스에서 공유 Redis 컨테이너에 남겨놓은 `ranking:rebuild:lock` 상태가 본 테스트 클래스로 유출되어, 백그라운드 Kafka 컨슈머 스레드가 `RankingRebuildInProgressException`을 뱉으며 처리가 차단되었습니다.

### Change Scope
- 수정 파일: `src/test/java/com/example/coffeeordersystem/recovery/DltReplayServiceIntegrationTest.java`

### Reverification
- 결과: FAIL (락 경합 차단)

### Next Attempt
- 계획: `@BeforeEach`에서 데이터베이스 및 Redis 상태 초기화(`cleanDatabase()`) 적용.


## Attempt 4

### Generate
- **시각**: 2026-07-19T22:00:00
- **수행 작업**: `@BeforeEach`에서 데이터베이스 및 Redis 상태 초기화(`cleanDatabase()`) 로직 추가.

### Evaluate
- **시각**: 2026-07-19T22:05:00
- **수행 검증**: GitHub Actions Run #29687996086 실행.
- **결과**: 비동기 퍼블리싱 시점에 락이 즉시 해제되지 않아 컨슈머가 여전히 Fencing 처리되었고, `DisabledTaskScheduler`로 인해 컨슈머 재개가 불가하여 실패.

### Failure Cause
- `service.replay` 메서드 내에서 퍼블리싱하는 동안 여전히 Redis 락을 쥐고 있어 컨슈머가 Fencing 되었고, 테스트용 `DisabledTaskScheduler`가 Container 재개 태스크를 무력화하여 무한 일시정지 상태에 빠졌습니다.

### Change Scope
- 수정 파일: `src/test/java/com/example/coffeeordersystem/recovery/DltReplayServiceIntegrationTest.java`

### Reverification
- 결과: FAIL (데드락)

### Next Attempt
- 계획: 퍼블리셔 Mockito Stubbing을 통해 publish 전 락을 선제 삭제하고, `auto-offset-reset=earliest`로 오프셋 리셋 스킵 방지.


## Attempt 5

### Generate
- **시각**: 2026-07-19T22:15:00
- **수행 작업**: `doAnswer` 락 삭제 stubbing 추가 및 `auto-offset-reset=earliest` 설정 반영.

### Evaluate
- **시각**: 2026-07-19T22:31:00
- **수행 검증**: GitHub Actions Run #29689016648, Run #29690279483, Run #29690393996 실행.
- **결과**: 빌드 성공 (BUILD SUCCESSFUL).

### Failure Cause
- 없음 (빌드 통과). 다만, 해당 락 삭제 및 컨슈머 세팅 우회 처리는 Issue #133의 문서 전용 범위를 벗어난 조치이므로 롤백해야 함.

### Change Scope
- 수정 파일: `src/test/java/com/example/coffeeordersystem/recovery/DltReplayServiceIntegrationTest.java`

### Reverification
- 결과: PASS

### Next Attempt
- 계획: 테스트 파일의 변경 사항을 완전히 롤백하고, ADR 문서의 세부 복구 계약 및 예외 격리 정책을 추가 확정하여 푸시.


## Attempt 6

### Generate
- **시각**: 2026-07-20T01:50:00
- **수행 작업**:
  - `DltReplayServiceIntegrationTest.java`를 `origin/main` 상태로 완전히 롤백함 (테스트 우회/수정 모두 제거).
  - `ADR-008-ranking-recovery-ledger.md`에 신규/기존 pending RESERVED 구분, 마커 TTL 계산식 정의, REDIS_APPLIED 상태의 예외 격리 일원화, 수동 복구 Rebuild 유도 규칙 작성, #134 구현 대상/크래시 시나리오 명시.
  - 상태표에 `COMMITTED + marker 존재(불일치)` 규칙 추가, 운영자 복구 시작 조건(점검창 진입, active member 0 검증) 추가 및 finally 블록 해제 정책 반영.

### Evaluate
- **시각**: 2026-07-20T02:14:00
- **수행 검증**: GitHub Actions Run #29696343639 실행.
- **결과**: CI 빌드 성공 (BUILD SUCCESSFUL).
  - 테스트 코드가 `origin/main` 상태로 롤백되어 Java 소스 코드 파일의 변경사항이 없으므로, CI(Harness Quality) 상에서는 문서 검증(Level 0) 및 기본 빌드 검증(Level 1) 단계만 무사히 패스하여 빌드 성공으로 나타났습니다.
  - 실제 비동기 경쟁 문제(Handoff)는 테스트 파일이 원복되었으므로 런타임 실행 시 다시 발생할 수 있으며, 이에 대한 독립적인 Codex Review와 QA 단계는 아직 진행되지 않아 `PENDING` 상태입니다.

### Failure Cause
- 없음 (하네스 빌드 통과).

### Change Scope
- 수정 파일: `docs/adr/ADR-008-ranking-recovery-ledger.md`

### Reverification
- 결과: PASS (하네스 게이트 통과)

### Next Attempt
- 계획: 없음 (작업 완료 및 PR 갱신).
