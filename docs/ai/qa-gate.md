# QA Gate

QA Agent는 `STRICT` 작업에서 Dev와 독립적으로 검증을 실행하고 결과만 기록합니다. 실행 시점은 Dev의 변경과 focused test가 끝난 뒤, Docs Agent의 evidence 확정 전입니다. 역할과 쓰기 금지는 [오케스트레이션 정책](orchestration-policy.md)을, 검증 Level과 실행 소유권은 [테스트 전략](../testing/test-strategy.md)을 따릅니다.

## 확인 항목

- Issue 요구사항과 변경된 API 계약, 도메인 정책, evidence가 연결되어 있는지 확인합니다.
- Mock 또는 Unit 결과는 API, DB, Kafka, Redis, Redisson, DLT의 실제 검증을 대체하지 않는다고 명시합니다.
- API 동작 또는 런타임 설정이 바뀌면 Level 5 로컬 기동과 Level 6 실제 HTTP 요청이 필요한지 Issue evidence의 YES/NO 결정과 이유를 확인합니다.
- DB, 트랜잭션, JPA, 락, 동시성이 바뀌면 Level 3 DB 통합 검증을 확인합니다.
- Kafka, Redis, Redisson, DLT가 바뀌면 Level 4 인프라 통합 검증을 확인합니다.
- 실제 HTTP 요청에서 예상하지 못한 500이 발생하거나, 애플리케이션 로그에 처리되지 않은 예외가 있으면 PASS로 판정하지 않습니다.
- 필요한 명령 결과, API 응답, DB 조회, 인프라 관찰, 실패 재현 정보가 evidence에 없으면 evidence 누락으로 판정합니다.

## 판정

| 결과 | 기준 |
| --- | --- |
| PASS | 필요한 검증이 완료되고 관찰 가능한 evidence가 있으며 예상하지 못한 500 또는 로그 예외가 없습니다. |
| FAIL | 검증 실패, 예상하지 못한 500, 처리되지 않은 로그 예외, 또는 수정 가능한 evidence 누락이 있습니다. |
| BLOCKED | 필요한 DB, 인프라, 권한, 환경이 없어 실행할 수 없고 대체 검증으로 결론을 낼 수 없습니다. |
| PARTIAL | 일부 검증만 완료했습니다. 미실행 Level, 이유, 남은 위험을 기록하며 완료 근거로 사용하지 않습니다. |

QA Agent는 production, test, docs 파일을 수정하지 않습니다. FAIL은 재현 명령과 허용된 수정 범위를 Dev Agent에게 반환하고, 현재 Issue 범위 밖의 결함 또는 정책 미결정은 Follow-up Issue 후보로 기록합니다.
