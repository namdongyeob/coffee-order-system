# Issue #27 Manual QA

## 판정

PARTIAL. 이 Issue는 문서와 저장소 운영만 변경하며 Level 5 로컬 기동과 Level 6 실제 HTTP 요청은 필요하지 않습니다. 실제 API, DB, Kafka, Redis, Redisson, DLT 검증은 수행하지 않았습니다.

## 확인 범위

- Review Gate와 QA Gate가 별도 문서로 존재하고, Review와 QA의 직접 수정 금지가 명시되어 있는지 확인합니다.
- QA Gate가 PASS, FAIL, BLOCKED, PARTIAL 및 예상하지 못한 500, 로그 예외, evidence 누락 기준을 포함하는지 확인합니다.
- PR template과 Evidence Guide가 `Related` 연결과 완료 주장 항목을 제공하는지 확인합니다.
- 삭제·이동된 파일의 참조와 Markdown 링크를 자동 검사합니다.

## 미실행 조건

- 애플리케이션 동작, API 계약, 런타임 설정, DB 또는 인프라 연결이 바뀌지 않아 실제 HTTP, DB, 인프라 관찰은 범위 밖입니다.
