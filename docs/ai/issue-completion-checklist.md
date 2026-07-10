# Issue 완료 전 체크리스트

모든 Issue 처리자는 PR을 열기 전에 이 목록을 확인합니다.

## 공통 체크리스트

- [ ] 관련 문서와 대상 Issue 본문을 읽었습니다.
- [ ] `acceptance-criteria.md`에 `Execution mode: SOLO|STANDARD|STRICT`와 비어 있지 않은 선택 이유를 기록했습니다.
- [ ] `acceptance-criteria.md`에 Level 5/6 필요 여부와 이유를 기록했습니다.
- [ ] `attempt-log.md`에 실패 원인, 수정 범위, 재검증 결과, 다음 입력을 기록했습니다.
- [ ] PR 본문에 읽은 문서 목록을 남겼습니다.
- [ ] PR 본문에 서브에이전트 사용 여부와 이유를 남겼습니다.
- [ ] Issue 범위 밖 구현이나 리팩터링을 하지 않았습니다.
- [ ] Controller, Service, Repository 책임이 섞이지 않았습니다.
- [ ] 필요한 테스트 또는 검증 명령을 실행했습니다.
- [ ] 선택한 execution mode의 필수 역할, 독립 검증, Docs 조건을 `docs/ai/orchestration-policy.md`와 일치시켰습니다.
- [ ] `STANDARD` 또는 `STRICT`에서 Main Coordinator가 파일 수정, 코드리뷰, 테스트, commit, push를 수행하지 않았습니다.
- [ ] GitHub Actions의 컴파일과 전체 테스트 결과를 확인했습니다.
- [ ] 작업 종류에 맞는 evidence 파일 또는 산출물을 남겼습니다.
- [ ] `docs/testing/verification-log.md`에 검증 결과를 기록했습니다.
- [ ] 새로 발견한 반복 실수나 주의점이 있으면 `docs/ai/agent-mistakes.md`에 기록했습니다.
- [ ] API, DB, 정책, 복구 방식이 바뀌면 관련 문서를 함께 갱신했습니다.
- [ ] PR 본문에 검증 결과와 남은 위험을 적었습니다.

## Execution Mode 확인

- [ ] `SOLO`는 애플리케이션 동작, build, runtime 변경이 없고 Solo Agent 한 명의 빠른 문서·하네스 검사만 수행했습니다.
- [ ] `STANDARD`는 제한된 단일 모듈 변경이며 STRICT 위험 조건이 없고 정책의 Combined Verifier와 CI 결과를 확인했습니다.
- [ ] `STRICT`는 schema, transaction, lock, concurrency, Kafka, Redis, Redisson, DLT, security, cross-module 또는 event contract, performance, recovery, harness 또는 workflow policy 위험을 분류하고 정책의 별도 Review, QA, Docs, CI 결과를 확인했습니다.

## 기록 기준

`docs/ai/agent-mistakes.md`에는 다음 경우만 기록합니다.

- 같은 실수가 다음 Issue에서도 반복될 가능성이 있습니다.
- 원인과 수정 방식이 확인되었습니다.
- 구현자나 리뷰어가 나중에 읽고 바로 피할 수 있는 내용입니다.

단순한 추측, 개인 감상, 이미 Issue 본문에 충분히 적힌 일반 요구사항은 기록하지 않습니다.

## 3계층 설계 확인

구현 Issue에서는 `docs/architecture/layered-design-policy.md`를 기준으로 다음을 확인합니다.

- Controller에 비즈니스 규칙이 들어가지 않았습니다.
- Service가 HTTP 요청/응답 객체에 직접 의존하지 않습니다.
- Repository가 비즈니스 결정을 하지 않습니다.
- 공통 유틸이나 Manager가 책임을 숨기는 우회 계층이 되지 않았습니다.

## 서브에이전트 기록

서브에이전트를 사용했다면 역할, 대상 Issue, 읽은 문서, 수행한 작업, 검증 결과를 PR 본문에 요약합니다.

서브에이전트를 사용하지 않았다면 `사용하지 않음`과 이유를 남깁니다. 예를 들어 단일 Issue의 파일 변경 범위가 작거나, DB schema와 JPA entity처럼 순서 의존성이 큰 작업이면 병렬화하지 않는 것이 맞습니다.

## Evidence 확인

Evidence는 `docs/testing/evidence-guide.md`를 따릅니다. 백엔드 작업은 스크린샷이 필수가 아니며, 테스트 로그, DB query 결과, API 응답, CLI output처럼 실제 동작을 관찰할 수 있는 자료를 남깁니다.
