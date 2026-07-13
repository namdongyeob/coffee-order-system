# Issue #52 Acceptance Criteria

Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/52

Execution mode: STRICT
Execution mode reason: 역할·실행 모드 판정·검증 소유권·merge 거버넌스의 workflow policy를 변경하므로 독립 Review, QA, CI가 필요합니다.
Level 5 required: NO
Level 5 reason: 애플리케이션 runtime과 인프라 연결을 변경하지 않습니다.
Level 6 required: NO
Level 6 reason: HTTP API와 실제 요청 계약을 변경하지 않습니다.

## 완료 기준

- [x] 첫 커밋 `707b233`이 `test-strategy.md`와 `orchestration-policy.md`의 회귀 검증 소유권 충돌만 수정했습니다.
- [x] 검증 실행 소유권의 단일 정본을 `test-strategy.md`로 확정하고 `orchestration-policy.md`는 규칙을 복제하지 않고 참조만 합니다.
- [x] CI run conclusion만 최종 PASS의 machine ground truth이며 로컬·Agent 로그가 이를 대체하지 않고 Level 3~6 실제 검증은 유지된다는 불변조건을 핵심 계약에 명시했습니다.
- [x] 모든 역할 공통 핵심 실행 계약을 24,284 → 15,226바이트로 축소했습니다. 12,288 목표는 사용자 합의로 면제했고, 남은 본문은 모드·역할·동시성·거버넌스·동결 등 축소 불가한 핵심 안전 규칙입니다.
- [x] 자율 큐 runbook 등 조건부 항목을 분리하고 Context Router에 조건부 문서의 독자와 진입 조건을 명시했습니다. 기본 역할 packet에 조건부 runbook을 포함하지 않습니다.
- [x] 개인/팀 역할과 AI 1차 결함 탐지·사람 최종 승인 책임을 구분했습니다.
- [x] 핵심 실행 계약의 특정 모델 제품명이 0건입니다.
- [x] SOLO/STANDARD/STRICT를 파일 위치가 아니라 판정 의미로 구분하고 `STRICT-lite` 등 새 실행 등급을 추가하지 않았습니다.
- [x] transaction·lock·concurrency·event contract·security와 Level 3~6 안전 규칙을 이동·완화 없이 유지했습니다.
- [x] 새 gate·receipt·Level 매핑 자동화를 추가하지 않았습니다.
- [x] #53·#56은 #52 본문에 흡수 근거를 남기고 종료됐습니다.
- [x] 기본 evidence 파일과 `verification.md`를 작성했습니다.

검증 실행 head는 `30ecf80`입니다. fresh 독립 Review는 `30ecf80`에서 `APPROVED`, 독립 QA는 `30ecf80`에서 `PASS`입니다. CI 상태는 GitHub 정본에서 별도로 확인하며 이 문서의 완료 기준 판정에 복제하지 않습니다.
