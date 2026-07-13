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
- Dev는 변경 범위 focused test를 기본 실행합니다. DB migration, 공통 transaction, event payload, Kafka 공통 consumer 설정, security, build/test infrastructure처럼 영향 범위가 넓은 변경만 push 전 로컬 전체 회귀(Windows: `.\gradlew.bat test --no-daemon`, macOS·Linux: `./gradlew test --no-daemon`)를 실행합니다. 그 외 변경의 전체 Level 1 회귀는 GitHub Actions `quality-gates`가 최종·단독으로 판정합니다.
- Level 1 전체 회귀 smoke는 전체 suite 상태를 기록할 뿐이며 Level 2, Level 3, Level 4의 focused evidence를 대체하지 않습니다.
- 문서·Issue 템플릿만 바꾼 PR은 로컬에서 하네스 테스트와 링크 검사를 우선하고, 전체 Gradle 테스트는 GitHub Actions 결과로 확인할 수 있습니다. workflow나 검증 스크립트를 바꾼 PR은 로컬에서도 관련 전체 검증을 한 번 실행합니다.
- Legacy evidence 인정과 backfill은 [Evidence Guide](evidence-guide.md)의 단일 정본을 따릅니다. 이 문서는 Legacy 여부를 별도로 판정하지 않습니다.
- 전체 테스트가 느리거나 불안정하면 focused test 결과와 함께 원인, 재현 명령, 남은 미검증 항목을 evidence에 남깁니다.

## 실행 모드와 테스트 수준

실행 모드 선택과 역할 구성은 `docs/ai/orchestration-policy.md`의 실행 모드 표만 따릅니다. 이 문서는 그 역할이 실행할 검증 수준과 명령만 정의합니다. 선택한 mode는 필요한 검증 Level을 낮추지 않으며 transaction, lock, Kafka, Redis, Redisson, DLT, runtime 변경의 Level 3~6 검증은 Issue evidence에 결정과 이유를 남깁니다.

## 에이전트별 검증 분담

- Dev Agent는 자기 변경 범위의 focused test를 실행합니다.
- Combined Verifier는 독립 focused verification을 실행합니다.
- Review Agent는 테스트를 재실행하지 않습니다. 대신 diff, 요구사항, 설계 경계, 테스트 케이스 누락을 검토합니다.
- QA Agent는 Dev와 독립적으로 필요한 focused test와 Level 3~6 실제 검증을 실행하고 결과를 보고합니다. STRICT에서 QA는 Level 1 전체 회귀 smoke를 로컬에서 재실행하지 않습니다.
- Dev가 PR 전 evidence를 완성한 뒤 metadata 불일치가 확인된 경우에만 Docs Agent가 확정된 검증 명령과 결과를 해당 Issue evidence와 `verification.md`에 옮깁니다. preflight가 current disposition·Attempt·head, acceptance checkbox, verification PASS, metrics 재시도 수의 모순을 먼저 fail-closed로 발견해야 하며 결과를 추측하거나 다시 실행하지 않습니다.
- Main Coordinator는 테스트를 실행하거나 결과 내용을 재판정하지 않고 선택된 모드의 독립 검증 보고와 GitHub Actions 상태의 존재만 확인합니다.
- GitHub Actions `quality-gates`가 컴파일과 전체 Level 1 회귀의 최종·단독 독립 기계적 gate입니다. CI가 unavailable, pending 또는 FAIL이면 QA의 focused 또는 Level 3~6 PASS로 대체할 수 없으며 PR은 blocked 상태를 유지합니다.
- 같은 워크스페이스에서 Gradle 테스트를 병렬 실행하지 않습니다. 병렬 실행이 필요하면 별도 worktree 또는 별도 build directory를 사용합니다.

## QA Level 1 경량화와 측정

STRICT에서 제거한 QA 로컬 전체 회귀 smoke의 대체 층은 모든 PR에서 같은 전체 suite를 실행하는 GitHub Actions `quality-gates`입니다. Dev의 broad-risk 변경 push 전 전체 회귀, QA의 focused 검증, Level 3~6 실제 검증, CI workflow 자체는 이 규칙으로 변경하지 않습니다.

후속 STRICT Issue의 `metrics.md`는 최소한 작업 시간, 재시도 수, Review 결함 수, QA 결함 수와 범위 밖 변경 파일 수를 고정 형식으로 기록합니다. 이 Issue의 비교 기준선은 #7 약 30분, #9 15분, #40의 초기 두 Attempt active duration 21분입니다. 실제 비용 감소 평가는 후속 Issue의 측정값만으로 수행하며, 추정값을 새 metrics에 기록하지 않습니다.

## k6 우선순위

1. Load Test.
2. Stress Test.
3. Spike Test.
4. Soak Test는 문서상 후보로만 둡니다.

## 완료 규칙

Mock 테스트는 DB, Kafka, Redis, 로컬 실행, 실제 API 검증을 대체하지 않습니다.

모든 Issue는 구현 전에 Level 5와 Level 6 필요 여부를 YES/NO로 결정하고 이유를 `docs/testing/evidence/issue-{number}/acceptance-criteria.md`에 기록합니다. API 동작, 런타임 설정, 인프라 연결이 바뀌면 기본값은 YES입니다. 문서·저장소 운영만 바뀌어 실제 API 경로가 없으면 NO로 결정할 수 있습니다.
