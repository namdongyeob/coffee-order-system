# Issue #7 Commands

## TDD RED

```powershell
.\gradlew.bat test --tests com.example.coffeeordersystem.order.service.OrderServiceLockTest --no-daemon
```

- 목적: 락 획득 실패가 `ORDER_LOCK_NOT_ACQUIRED`로 변환되는 계약의 변경 전 실패 확인.
- 결과: RED. `ErrorCode.ORDER_LOCK_NOT_ACQUIRED`가 없어 `compileTestJava`가 예상 이유로 실패했습니다.
- 소요 시간: 33초.

## TDD GREEN

```powershell
.\gradlew.bat test --tests com.example.coffeeordersystem.order.service.OrderServiceLockTest --no-daemon
```

- 목적: `lock:order:user:{userId}`, 2초 대기, 5초 lease와 획득 실패 예외의 최소 구현 확인.
- 결과: PASS.
- 소요 시간: 29초.

## Focused Unit And Controller

```powershell
.\gradlew.bat test --tests com.example.coffeeordersystem.order.service.OrderServiceLockTest --tests com.example.coffeeordersystem.order.controller.OrderControllerTest --no-daemon
```

- 목적: 잠금 성공·트랜잭션 실패 시 해제와 락 획득 실패 HTTP 409 응답 계약 확인.
- 최초 결과: 테스트 코드의 누락 import와 `OrderResponse` 인자 오류로 컴파일 실패했으며 production 결함은 아니었습니다.
- 수정 후 결과: PASS.
- 최종 소요 시간: 33초.

## Level 4 Redis Testcontainers

```powershell
.\gradlew.bat test --tests com.example.coffeeordersystem.RedisOrderLockIntegrationTest --no-daemon
```

- 목적: 실제 Redis 컨테이너에서 다른 스레드가 동일 사용자 락을 점유하면 약 2초 후 획득 실패하는지 확인.
- 결과: PASS.
- 소요 시간: 1분 19초.

## Level 1 Full Smoke

```powershell
.\gradlew.bat test --no-daemon
```

- 목적: production/test 변경과 기존 DB 비관적 락, 주문 트랜잭션, 전체 suite 회귀 확인.
- 결과: PASS.
- 소요 시간: 1분 17초.

## Static Checks

```powershell
codex --version
git diff --check
```

- 결과: `codex-cli 0.141.0`; `git diff --check` 오류 없음. Windows LF→CRLF 경고만 관찰했습니다.

## Repository Harness Precheck

```powershell
python scripts/harness_gate.py --issue 7 --base-ref origin/main --check-links --include-worktree
```

- 결과: FAIL. Dev 범위의 evidence와 링크가 아니라 Docs Agent 소유 `verification-log.md`의 Issue #7 행, 그리고 아직 독립 QA가 실행하지 않은 필수 Level 5/6 PASS가 없음을 정확히 거부했습니다.
- 처리: Dev가 결과를 추측해 `verification-log.md`를 수정하지 않습니다. 독립 QA의 Level 5/6 결과와 Docs Agent 반영 후 같은 명령을 재실행해야 합니다.

## Independent QA Verification

```powershell
.\gradlew.bat test --tests com.example.coffeeordersystem.RedisOrderLockIntegrationTest --no-daemon
.\gradlew.bat test --no-daemon
```

- Level 4 결과: PASS. 실제 `redis:7.4.2`, 1 test, failures 0, errors 0, BUILD SUCCESSFUL 1분 15초, test 53.107초.
- Level 1 결과: PASS. 전체 25 tests, failures 0, errors 0, skipped 0, BUILD SUCCESSFUL 1분 23초.
- Level 5/6: 독립 QA 역할 보고의 로컬 앱·인프라 기동 및 실제 HTTP/DB 관찰 절차로 PASS했습니다. 역할 보고에 정확한 실행 명령은 제공되지 않아 추측해 기록하지 않으며 관찰 결과는 `manual-qa.md`에 기록합니다.
- 정리: QA가 MySQL, Redis, 애플리케이션 리소스와 사용 포트를 정리했습니다.
- CI: remote branch와 PR이 아직 없어 pending입니다.
