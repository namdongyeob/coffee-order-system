# 테스트 전략

## 필수 검증

| Level | 대상 |
| --- | --- |
| Level 0 | 문서, 정적 검사, 저장소 하네스, 검증 도구 자체. |
| Level 1 | 빌드, Unit 테스트, 전체 회귀 smoke. |
| Level 2 | Controller/API 요청·응답·검증·에러 계약. |
| Level 3 | DB, 트랜잭션, JPA, 락, 동시성 통합. |
| Level 4 | Kafka, Redis, Redisson, DLT 인프라 통합. |
| Level 5 | 로컬 애플리케이션 기동. |
| Level 6 | 실제 Postman, curl, http 요청. |
| Level 7 | k6 Load, Stress, Spike 관찰. |

`docs/testing/evidence/issue-{number}/verification.md`의 Level 열에는 위 표의 `Level 0`부터 `Level 7`까지만 기록합니다. 한 행에는 하나의 Level만 기록하고 세부 대상은 검증 범위 열에 적습니다. 결과 열의 허용값은 `PASS`, `FAIL`, `PARTIAL`이며 완료 근거에는 `PASS`만 사용합니다. 전역 뷰는 [Evidence Guide](evidence-guide.md)의 on-demand 명령으로만 재현하고 Git에 커밋하지 않습니다.

## 실행 속도와 신뢰성 기준

- 개발 중에는 변경 범위에 맞는 focused test를 먼저 실행합니다.
- 작은 Controller API는 `@WebMvcTest`와 `MockMvc`로 HTTP mapping, status, response body를 검증합니다.
- DB schema, JPA mapping, transaction, lock이 핵심인 Issue는 Level 3 DB Integration으로 검증합니다.
- Kafka, Redis, Redisson, DLT가 핵심인 Issue는 Level 4 Infra Integration으로 검증합니다.
- Testcontainers는 필요한 검증 레벨에서만 사용합니다. Controller 계약만 확인하는 Issue에 무거운 full context 테스트를 기본값으로 두지 않습니다.
- Dev는 변경 범위 focused test를 기본 실행합니다. DB migration, 공통 transaction, event payload, Kafka 공통 consumer 설정, security, build/test infrastructure처럼 영향 범위가 넓은 변경만 push 전 로컬 전체 회귀(Windows: `.\gradlew.bat test --no-daemon`, macOS·Linux: `./gradlew test --no-daemon`)를 실행합니다. source/test/build/runtime 변경의 전체 Level 1 회귀는 GitHub Actions 고정 `quality-gates` job이 최종 source SHA에서 판정합니다.
- Level 1 전체 회귀 smoke는 전체 suite 상태를 기록할 뿐이며 Level 2, Level 3, Level 4의 focused evidence를 대체하지 않습니다.
- PR body `edited`는 source required check와 다른 `metadata-gates` 이름·concurrency에서 Python 하네스·링크 검사만 실행해 진행 중 source run을 취소하거나 그 판정을 대체하지 않습니다. docs/evidence-only와 workflow·검증 스크립트 변경도 source/test/build/runtime이 없으면 Gradle을 요구하지 않습니다. 링크 검사는 한 번, Gradle은 컴파일 의존성을 포함한 `test` 한 invocation만 실행합니다.
- Legacy evidence 인정과 backfill은 [Evidence Guide](evidence-guide.md)의 단일 정본을 따릅니다. 이 문서는 Legacy 여부를 별도로 판정하지 않습니다.
- 전체 테스트가 느리거나 불안정하면 focused test 결과와 함께 원인, 재현 명령, 남은 미검증 항목을 evidence에 남깁니다.

## 실행 모드와 테스트 수준

실행 모드 선택과 역할 구성은 `docs/ai/orchestration-policy.md`의 실행 모드 표만 따릅니다. 이 문서는 그 역할이 실행할 검증 수준과 명령만 정의합니다. 선택한 mode는 필요한 검증 Level을 낮추지 않으며 transaction, lock, Kafka, Redis, Redisson, DLT, runtime 변경의 Level 3~6 검증은 Issue evidence에 결정과 이유를 남깁니다.

## 에이전트별 검증 분담

- Dev Agent는 자기 변경 범위의 focused test를 실행합니다.
- Combined Verifier는 독립 focused verification을 실행합니다.
- Review Agent는 테스트를 재실행하지 않습니다. 대신 diff, 요구사항, 설계 경계, 테스트 케이스 누락을 검토합니다.
- QA Agent는 Dev와 독립적으로 필요한 focused test와 Level 3~6 실제 검증을 실행하고 결과를 보고합니다. STRICT에서 QA는 Level 1 전체 회귀 smoke를 로컬에서 재실행하지 않습니다.
- Dev가 PR 전 evidence를 완성한 뒤 metadata 불일치가 확인된 경우에만 Docs Agent가 확정된 검증 명령과 결과를 해당 Issue evidence와 `verification.md`에 옮깁니다. preflight는 기본 Acceptance Criteria·verification과 존재하는 상세 evidence의 모순을 fail-closed로 발견하며 결과를 추측하거나 다시 실행하지 않습니다.
- Main Coordinator는 테스트를 실행하거나 결과 내용을 재판정하지 않고 선택된 모드의 독립 검증 보고와 GitHub Actions 상태의 존재만 확인합니다.
- GitHub Actions의 고정 `quality-gates` job이 최종·단독 독립 기계 gate입니다. 분류기가 `requires_java_ci=true`로 판정한 source/test/build/runtime 변경은 그 job에서 전체 Level 1을 실행합니다. CI가 unavailable, pending, 다른 source SHA 또는 FAIL이면 QA PASS로 대체할 수 없습니다.
- 같은 워크스페이스에서 Gradle 테스트를 병렬 실행하지 않습니다. 병렬 실행이 필요하면 별도 worktree 또는 별도 build directory를 사용합니다.

## QA Level 1 경량화와 재실행

STRICT에서 제거한 QA 로컬 전체 회귀 smoke의 대체 층은 모든 PR에서 같은 전체 suite를 실행하는 GitHub Actions `quality-gates`입니다. Dev의 broad-risk 변경 push 전 전체 회귀, QA의 focused 검증, Level 3~6 실제 검증, CI workflow 자체는 이 규칙으로 변경하지 않습니다.

Gradle·Docker·Level 3~7은 `source/test/runtime 입력 + 정규화 명령 + 환경 profile`이 같고 PASS가 있으면 재사용합니다. 입력 변경, 이전 FAIL 진단, 명시적 flaky 격리, 분류기 stale, 독립 QA 최종 증명만 재실행 근거이며 `verification.md` 또는 실패한 `attempt-log.md`에 이유를 남깁니다. metrics는 완료 증명이 아닙니다.

## k6 우선순위

1. Load Test.
2. Stress Test.
3. Spike Test.
4. Soak Test는 문서상 후보로만 둡니다.

과거 실측 결과는 [k6 결과](../performance/k6-results.md)를 참고합니다.

## 완료 규칙

Mock 테스트는 DB, Kafka, Redis, 로컬 실행, 실제 API 검증을 대체하지 않습니다.

Level 3~7 evidence는 검증한 source-tree SHA를 기록합니다. evidence-only commit SHA가 전진해도 source-tree SHA가 같으면 runtime evidence를 유지하고, 분류기가 runtime stale로 판정한 source/test/build/runtime 변경에서는 재검증합니다.

모든 Issue는 구현 전에 Level 5와 Level 6 필요 여부를 YES/NO로 결정하고 이유를 `docs/testing/evidence/issue-{number}/acceptance-criteria.md`에 기록합니다. API 동작, 런타임 설정, 인프라 연결이 바뀌면 기본값은 YES입니다. 문서·저장소 운영만 바뀌어 실제 API 경로가 없으면 NO로 결정할 수 있습니다.
