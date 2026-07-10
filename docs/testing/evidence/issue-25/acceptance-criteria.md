# Issue #25 Acceptance Criteria

Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/25

Execution mode: STRICT
Execution mode reason: 하네스 완료 판정, verification-log 스키마, 검사 코드와 테스트를 함께 변경하는 workflow policy 작업입니다.

Level 5 required: NO
Level 5 reason: Java 애플리케이션 런타임과 API 동작을 변경하지 않는 저장소 하네스 작업입니다.

Level 6 required: NO
Level 6 reason: 실제 HTTP API 계약이나 요청 경로를 변경하지 않는 저장소 하네스 작업입니다.

## Completion Criteria

- [ ] Level 0~7의 의미가 서로 겹치지 않게 정의되었습니다.
- [ ] verification-log의 Level은 정확히 `Level 0`~`Level 7`만 허용합니다.
- [ ] 한 행에는 하나의 Level만 존재하며 복합 표기가 거부됩니다.
- [ ] 결과는 `PASS|FAIL|PARTIAL`만 허용합니다.
- [ ] `PARTIAL`은 기록할 수 있지만 필수 Level 완료 근거로 인정되지 않습니다.
- [ ] 기존 verification-log 모든 데이터 행이 새 형식으로 정규화되었습니다.
- [ ] `Level 5 required: YES`이면 동일 Issue의 Level 5 PASS가 없을 때 실패합니다.
- [ ] `Level 6 required: YES`이면 동일 Issue의 Level 6 PASS가 없을 때 실패합니다.
- [ ] required가 NO인 Level은 PASS 행을 강제하지 않습니다.
- [ ] 다른 Issue의 PASS 또는 다른 Level의 PASS로 교차 검사를 우회할 수 없습니다.
- [ ] 형식·교차 검사에 성공/실패 단위 테스트가 있습니다.
- [ ] `docs/ai/issue-completion-checklist.md`의 공통 항목을 확인했습니다.
- [ ] `docs/testing/evidence-guide.md`의 기본 evidence 파일을 작성했습니다.

## Verification Plan

- 새 테스트가 기존 구현에서 의도대로 실패하는지 확인합니다.
- `python -m unittest discover -s scripts/tests -p "test_*.py"`
- `python scripts/harness_gate.py --issue 25 --base-ref origin/main --check-links`
- 의도된 잘못된 Level, 복합 Level, required YES/PASS 누락 fixture가 실패하는지 확인합니다.
- `git diff --check`
