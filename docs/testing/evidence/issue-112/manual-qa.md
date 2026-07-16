# Issue #112 Manual QA

Issue: #112
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/112
Date: 2026-07-16

## 실제 환경

- `docker/compose.yaml`의 MySQL, Kafka, Redis를 `up -d --wait`로 기동했고 세 서비스가 healthy였습니다.
- 로컬 애플리케이션 기동 시 Flyway V1~V6 적용을 확인했습니다.
- 사용자 6112를 10000 충전하고 메뉴 1 주문을 생성했습니다. API 결과는 orderId 1, PAID였고 DB PAID 건수는 1이었습니다.
- Kafka processed event id는 `ae9811d8-8868-4c96-afec-95af9d279db4`, 초기 Redis 일간 ranking의 메뉴 1 score는 1이었습니다.

## 최초 Rebuild

- 정상 애플리케이션을 종료하고 normal consumer group active member가 없으며 current offset 1, end 1, lag 0임을 확인했습니다.
- live ranking을 삭제하고 ledger/run이 0인 상태에서 Rebuild runner를 실행했습니다.
- 완료 로그는 `inputRecords=1 uniqueEvents=1 conflicts=0`이었습니다.
- ledger는 event 한 행, state `COMMITTED`, source `REBUILD`, fingerprint `78d2312376c89b6edbeea831e84b1e4916555331710930a034256be4d8259799`였습니다.
- run 1과 run-event 1이 완료됐고 Redis score 1, rebuild lock 0, 임시 key 0이었습니다.

## 동일 이벤트 재실행

- 같은 Kafka 이벤트로 Rebuild runner를 다시 실행했습니다.
- 완료 로그는 다시 `inputRecords=1 uniqueEvents=1 conflicts=0`이었습니다.
- ledger는 여전히 한 행, distinct fingerprint 한 종류, state/source는 `COMMITTED`/`REBUILD`였습니다.
- completed run은 2, run-event는 2가 됐지만 live Redis score는 1이어서 양방향 중복 집계가 없었습니다.
- rebuild lock 0, normal group active member 없음, current=end=1, lag 0이었습니다.

## Pending 복구

- 최신 run `344f1b8e-a813-4df9-833a-c276ceb88690`을 `SWAPPED_PENDING_LEDGER`로 변경하고 completed_at과 해당 ledger 행을 비운 상태를 조성했습니다.
- 다음 runner 완료 로그는 `inputRecords=0 uniqueEvents=0 conflicts=0`이어서 replay와 새 swap 없이 backfill-only로 복구됐습니다.
- ledger의 rebuild_run_id는 조성한 pending run id와 같았고 state/source는 `COMMITTED`/`REBUILD`였습니다.
- run 총수는 2로 유지됐고 둘 다 COMPLETED, run-event 2, Redis score 1, lock 0, 임시 key 0이었습니다.

## 관찰 구분과 cleanup

- Actuator health는 `DiskSpaceHealthIndicator`가 subst 드라이브 `U:\`의 free space를 0으로 읽어 HTTP 503을 반환했습니다. 실제 C: free space는 39,930,208,256 bytes였고 API·DB·Kafka·Redis E2E와 Rebuild 검증은 성공했습니다. 따라서 health endpoint PASS로 표현하지 않습니다.
- Rebuild ApplicationRunner는 기능 완료 후에도 non-web process로 대기하므로 완료 로그와 DB·Redis evidence 뒤 해당 runner PID만 수동 종료했습니다. Ctrl+C 때문에 Gradle wrapper exit 1이 기록됐지만 완료 전 기능 예외나 rollback은 없었습니다.
- 최종 `docker compose down` 뒤 project container와 network가 제거됐고 `docker ps` 잔여 container는 0개였습니다.

## 후속 판정

- 독립 Review, 독립 QA, 최신 GitHub Actions CI는 아직 Dev evidence에 포함하지 않으며 Draft PR의 GitHub 정본에서 확인합니다.
