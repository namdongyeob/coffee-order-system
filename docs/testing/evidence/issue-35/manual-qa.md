# 수동 검토

- PASS. `STANDARD`가 draft PR을 먼저 만들 수 있으나 외부 독립 리뷰 지연 시 내부 독립 Combined Verifier를 요구합니다.
- PASS. Combined Verifier와 CI가 모두 PASS하기 전 ready·완료·merge 권고를 금지하고, 결과를 PR 본문 또는 evidence에 반영하도록 규정합니다.
- PASS. 하네스·스크립트 hot path가 필수 문서 4개와 조건부·제외·추가 탐색 규칙을 각각 하나씩 갖습니다.
- PASS. Review Gate와 QA Gate의 판정 의미를 바꾸는 작업은 `STRICT`이며 링크·오탈자 수정은 제외됩니다.
- PASS. 독립 Review는 결함 0건, 독립 QA는 결함 0건으로 종료했습니다. QA 최초 loader error는 존재하지 않는 클래스명을 지정한 명령 오류였고, 실제 `OrchestrationContractTest` 2건은 PASS했습니다.

애플리케이션, DB, Kafka, Redis와 HTTP API를 변경하지 않아 Level 5와 Level 6 수동 검증은 수행 대상이 아닙니다. GitHub Actions CI는 pending입니다.
