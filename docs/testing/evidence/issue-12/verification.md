# 검증 로그

| 날짜 | Issue | Level | 결과 | 검증 범위 | 명령/Evidence | 비고 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-12 | Issue #12 HTTP 요청 산출물 | Level 5 | PASS | clean Compose와 local profile 애플리케이션 기동 | `docs/testing/evidence/issue-12/manual-qa.md` | MySQL·Redis·Kafka healthy, 앱 20.456초 기동, health 200 `UP`을 확인하고 앱과 프로젝트 Compose·volume을 정리했습니다. |
| 2026-07-12 | Issue #12 HTTP 요청 산출물 | Level 6 | PASS | 실제 성공·실패 API 요청·응답 | `http/issue-12-api-validation.http`; `docs/testing/evidence/issue-12/manual-qa.md` | 메뉴 200, 충전 200, 주문 201, 인기 메뉴 200과 잔액 부족 409, 없는 메뉴 404 원문을 고정 QA ID 1201~1203으로 확인했습니다. |
| 2026-07-12 | Issue #12 repository verification | Level 0 | PASS | 문서·정적·repository gate | `python scripts/harness_gate.py --issue 12 --branch codex/issue-12-http-artifacts --base-ref origin/main --check-links`; `git diff --check`; base 대비 변경 경로 검사 | Issue #12 evidence, 검증 로그와 HTTP 산출물만 변경된 범위를 확인했고 repository gate와 diff 정적 검사가 PASS했습니다. |
