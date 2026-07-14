# Issue #91 Acceptance Criteria

Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/91

Execution mode: STRICT
Execution mode reason: 새 오케스트레이션 도구가 workflow policy(병렬 Dev 슬롯·소유권 규칙)를 코드화합니다. harness core는 변경하지 않지만 병렬 실행 규칙 자체를 실행 가능한 형태로 만드므로 독립 Review·QA가 필요합니다.
Level 5 required: NO
Level 5 reason: 애플리케이션 런타임을 변경하지 않습니다(도구는 애플리케이션과 독립된 Python CLI입니다).
Level 6 required: NO
Level 6 reason: HTTP 계약을 변경하지 않습니다.

## 범위 결정

사용자에게 "1차 범위 그대로 한 PR"과 "설계 문서 먼저 분리" 중 선택을 물었고, **1차 범위 그대로 한 PR**로 진행하기로 확정했습니다. Issue 본문의 1차 범위(Dev 병렬만, 3번째 Dev 예외 제외, 2차 Review∥QA 실험은 이 Issue 밖)를 그대로 구현합니다.

## 완료 기준

- [x] 병렬 실행 도구가 하네스 core와 분리된 재사용 가능한 형태로 존재한다. `scripts/team_orchestration.py`는 `harness_gate.py`를 import하지 않고 독립적으로 동작하며(콘솔 인코딩 하드닝 함수만 동일 패턴으로 복제), 별도 CLI(`register`/`release`/`message`/`status`/`reset`)로 다음 프로젝트에 파일 하나로 이전 가능합니다.
- [x] max_active_agents/max_writer_agents/owned paths/메시지 4종/gitignored team-state 제약이 구현된다. `TeamOrchestrationConfig`(기본 3/2, ceiling 2), `owned_paths_overlap`(디렉터리 포함 관계 휴리스틱), `MESSAGE_TYPES`(FINDING/NEED/BLOCKED/SCOPE_CONFLICT 고정), `.team-orchestration-state/`(`.gitignore`에 추가)로 구현했습니다.
- [x] 자동 통합·자동 merge·무한 autofix가 없음을 확인한다. `NoAutoMergeInvariantTest`가 모듈에 merge/rebase/cherry-pick/autofix 관련 함수나 git 서브커맨드가 없음을 소스 스캔으로 검증하고, `metrics_snapshot()`의 `merge_conflicts`가 항상 0임을 확인합니다.
- [x] coffee 합성 독립 작업 2개로 smoke가 통과한다(속도 주장 아님). `SyntheticSmokeTest.test_two_non_overlapping_synthetic_dev_tasks_run_end_to_end`가 menu/point 두 비중첩 합성 Dev 작업을 등록→메시지 3건 교환→해제까지 end-to-end로 통과시키고, 반례로 겹치는 경로 합성 작업 2개가 SCOPE_CONFLICT로 막히는 것도 확인합니다. **이 smoke는 기능 안전성만 확인하며 속도·토큰 효과의 증거로 사용하지 않습니다**(Issue 본문 "측정 방식" 절 그대로).
- [x] 기존 Review→QA→CI→evidence 안전장치가 유지된다. 이 도구는 Review/QA 슬롯이나 merge 판단에 관여하지 않고 Dev 슬롯 배정만 다루며, fresh 독립 Review Agent와 fresh 독립 QA Agent를 이 Issue에도 그대로 배정했습니다.

검증 실행 head는 `040ed3319911b90dd9e6a7e6d53030112367dd83`입니다. fresh 독립 Review Agent는 이 head에서 `APPROVED`(P0/P1 없음, P2 2건 — owned_paths_overlap 휴리스틱 한계 문서화 권장은 모의투자 이전 준비 단계 후속 과제로 이월)이고, fresh 독립 QA Agent는 같은 head에서 `PASS`(P0/P1/P2 없음)입니다.
