# 검증 로그

Attempt: 1
Head: e09452e0d05b883606af7e6ae6bb38a500c67914

| 날짜 | Issue | Level | 결과 | 검증 범위 | 명령/Evidence | 비고 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-14 | Issue #104 Entity 컨벤션 통일 | Level 0 | PASS | Entity 5종 getter/생성자 시그니처 동일성, 문서·헤더 주석 정합성 | 독립 Combined Verifier subagent 실행(`docs/testing/evidence/issue-104/manual-qa.md`) | API/런타임 동작 변경 없는 내부 구현 치환이라 Level 5/6 대상 아님(acceptance-criteria.md 참고). |
| 2026-07-14 | Issue #104 Entity 컨벤션 통일 | Level 1 | PASS | 전체 회귀(76 tests) | `docker-verify` 클론(`C:\coffee-verify`, 브랜치 `claude/issue-104-entity-getter-protected-convention`)에서 `./gradlew.bat test` 실행, `build/test-results/test/*.xml` 27개 파일 집계 | 76 tests, failures 0, errors 0, skipped 0. 원래 작업 디렉터리(한글 경로)에서는 아래 환경 제약으로 실행 불가해 별도 경로에서 실행. |
| 2026-07-14 | Issue #104 Entity 컨벤션 통일 | Level 2 | PASS | `MenuControllerTest`(Controller/API 계약) | WSL Ubuntu 클론(`~/coffee-order-system-wsl`)에서 `./gradlew test --tests '*MenuControllerTest*'` | `TEST-...MenuControllerTest.xml`: tests=2, failures=0, errors=0. `getMenusReturnsSeedMenus`, `getPopularMenusReturnsRankedMenus` 모두 PASS. |
| 2026-07-14 | Issue #104 Entity 컨벤션 통일 | Level 4 | PASS | `OutboxEventIntegrationTest`(Kafka Testcontainers) | `C:\coffee-verify`(Docker Desktop 사용 가능한 비한글 경로)에서 `./gradlew.bat test --tests '*OutboxEventIntegrationTest*'` | `TEST-...OutboxEventIntegrationTest.xml`: tests=2, failures=0, errors=0. `publishPendingDeliversOutboxEventToKafkaAndMarksItPublished`, `createOrderSavesOutboxEventInOrderTransaction` 모두 PASS(실제 Kafka Testcontainers 사용). |

## 로컬 원본 작업 디렉터리의 Gradle 테스트 실행 제약(참고, 우회됨)

원래 작업 디렉터리 경로(`...\코드컨벤션`)에 한글이 포함되어 있고 이 Windows 머신의 "비유니코드 프로그램용 언어(시스템 로캘)"가 MS949로 설정되어 있어, Gradle 테스트 워커의 classpath argfile 경로 해석이 깨져 그 디렉터리에서는 모든 테스트가 `java.lang.ClassNotFoundException`으로 실행조차 되지 않았습니다(`git stash`로 `main` HEAD 원본에서도 재현, daemon 재시작·`--no-daemon`·UTF-8 codepage로도 해결 안 됨 — `sun.jnu.encoding`은 JVM 기동 시 로캘로 고정). 위 Level 1/2/4 행은 이 제약을 우회해 WSL Ubuntu 클론과 비한글 Windows 경로 클론(`C:\coffee-verify`, 같은 커밋을 fetch)에서 실제로 실행한 결과입니다. 로컬 pre-push harness gate는 이 재검증 전에 사용자 승인 하에 1회 `--no-verify`로 우회했었고, 이후 이 실행 결과로 evidence를 정본화했습니다.
