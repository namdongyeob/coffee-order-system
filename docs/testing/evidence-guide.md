# Evidence Guide

LazyCodex 원본의 evidence-bound workflow를 이 프로젝트에 맞게 적용합니다. 완료 주장은 테스트 통과 문장만으로 하지 않고, 작업 종류에 맞는 관찰 가능한 evidence를 함께 남깁니다.

## 저장 위치

Issue별 evidence는 다음 위치에 둡니다.

```text
docs/testing/evidence/issue-{number}/
```

## 기본 파일

| 파일 | 내용 |
| --- | --- |
| `commands.md` | 실행한 명령, 목적, 결과 요약. |
| `manual-qa.md` | 사람이 확인한 API 응답, DB query, CLI output, 스크린샷 위치. |
| `test-output.txt` | 필요한 경우 테스트 출력 원문 또는 핵심 발췌. |

## 작업별 Evidence

| 작업 | Evidence 후보 |
| --- | --- |
| DB migration | Flyway migration 파일, JPA 통합 테스트 결과, DB table/seed 조회 결과. |
| API | `.http` 파일, curl output, Postman collection, 응답 JSON. |
| Kafka/Redis | topic/key 확인 명령, Testcontainers 통합 테스트 결과, DLT 확인 결과. |
| k6 | script, summary output, 관찰 결과. |
| UI/TUI | screenshot, terminal capture, visual QA 메모. |

## PR 작성 기준

PR 본문에는 다음을 남깁니다.

- Automated verification.
- Manual QA.
- Adversarial QA.
- Cleanup receipt.
- Evidence files.

## 주의

- 비밀값, access token, 개인 정보가 들어간 output은 저장하지 않습니다.
- 스크린샷은 UI/TUI처럼 시각 검증이 필요한 경우에 우선 사용합니다.
- 백엔드 작업에서는 테스트 로그, API 응답, DB query 결과가 더 적절한 evidence일 수 있습니다.
