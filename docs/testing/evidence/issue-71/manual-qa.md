# Issue #71 Manual QA

- 작업 유형: workflow policy와 harness Level 0 계약.
- 자동 검증: focused 행위 계약, 전체 harness, repository gate, link check, diff check를 사용합니다.
- 수동 검증: 변경 파일 allowlist와 #60/#55 보존 여부를 diff로 확인합니다.
- Adversarial QA: 미래 Review·QA 링크 없이 pre-review가 가능하고, non-doc delta가 QA를 stale로 만드는 실패 경로를 계약 테스트로 검증합니다.
- 보존 확인: 기존 `OrchestrationContractTest`의 #60 고정 큐·조건부 merge 및 #55 경량 body·외부 임시 파일 preflight 계약이 전체 suite에서 함께 PASS했습니다.
- Cleanup: 애플리케이션, 컨테이너 또는 외부 runtime을 시작하지 않았으므로 정리 대상이 없습니다.
- 미검증: Level 5/6은 Issue 범위가 아니므로 실행하지 않습니다.
