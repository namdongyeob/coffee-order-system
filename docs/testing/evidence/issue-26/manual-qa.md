# Issue #26 Manual QA

## Applicability

- API manual QA: 적용하지 않음. API/HTTP 변경이 없습니다.
- DB manual QA: 적용하지 않음. DB 변경이 없습니다.
- UI manual QA: 적용하지 않음. UI 변경이 없습니다.
- Infra manual QA: 적용하지 않음. 런타임 또는 인프라 연결 변경이 없습니다.

## Level Decision

- Level 5: NO — 로컬 Java 애플리케이션 기동 검증 대상이 아닙니다.
- Level 6: NO — 실제 Postman, curl, http 요청 검증 대상이 아닙니다.

## Relevant Manual/Adversarial Observation

- temporary Router가 `missing.md`를 선언하면 harness CLI가 종료 코드 1을 반환하고 `missing.md`를 보고했습니다. 이는 파일 계약 누락을 거부하는 음성 경로 확인입니다.
- actual Router declared paths는 `[]`이며 harness CLI가 종료 코드 0을 반환했습니다.
