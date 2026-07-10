# 수동 검토

- `STANDARD`가 draft PR을 먼저 만들 수 있으나 외부 독립 리뷰 지연 시 내부 독립 Combined Verifier를 요구하는지 확인합니다.
- Combined Verifier와 CI가 모두 PASS하기 전 ready·완료·merge 권고를 금지하고, 결과를 PR 본문 또는 evidence에 반영하는지 확인합니다.
- 하네스·스크립트 hot path가 필수 문서 4개, 조건부·제외·추가 탐색 규칙을 각각 하나씩 갖는지 확인합니다.
- Review Gate와 QA Gate의 판정 의미를 바꾸는 작업이 `STRICT`이며 링크·오탈자 수정은 제외되는지 확인합니다.

애플리케이션, DB, Kafka, Redis와 HTTP API를 변경하지 않아 Level 5와 Level 6 수동 검증은 수행 대상이 아닙니다.
