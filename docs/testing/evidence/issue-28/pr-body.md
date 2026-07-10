## 요약

- ADR 001부터 007까지 상태·결정일, 맥락, 동인, 선택지, 결정 이유, 결과·단점, 재검토 조건을 보강했습니다.
- ADR-002, ADR-003, ADR-005에 장애 동작, 정합성 영향, 검증 계획, 운영 위험 비교를 추가했습니다.
- `docs/adr/README.md`에 Superseded 관계와 실제·계획 검증 표기 규칙을 추가했습니다.

Related: #28

## Execution mode

Execution mode: STANDARD
Execution mode reason: 기존 기술 결정을 변경하지 않고 ADR의 근거와 선택지를 보강하는 문서 작업이며, 독립 Combined Verifier와 CI로 사실성·링크·범위를 검증합니다.

Level 5 required: NO
Level 5 reason: 애플리케이션 런타임과 API 동작을 변경하지 않는 문서 작업입니다.
Level 6 required: NO
Level 6 reason: HTTP 계약과 실제 요청 경로를 변경하지 않는 문서 작업입니다.

## 검증

- Automated verification: `python -m unittest scripts.tests.test_harness_gate` 48 PASS, `python scripts/harness_gate.py --issue 28 --base-ref origin/main --check-links --include-worktree` PASS, `git diff --check` PASS, `.\gradlew.bat test --no-daemon` 종료 코드 0 PASS.
- Manual QA: Issue #28 완료 기준과 ADR별 core section, 관련 Issue·설계 링크, Superseded 규칙을 대조했습니다.
- Adversarial QA: ADR-002/003/005에 장애 동작·정합성 영향·검증 계획·운영 위험 열이 있는지 확인했고, 실제 근거와 계획된 검증을 분리했습니다.
- Cleanup receipt: production, test, build, infra 파일 변경 없음. 변경 범위는 `docs/adr/*`, `docs/testing/evidence/issue-28/*`, `docs/testing/verification-log.md`입니다.

## Evidence

- `docs/testing/evidence/issue-28/acceptance-criteria.md`
- `docs/testing/evidence/issue-28/attempt-log.md`
- `docs/testing/evidence/issue-28/commands.md`
- `docs/testing/evidence/issue-28/manual-qa.md`

## 역할과 후속 Gate

- 읽은 문서: Issue #28, requirements, scope, concurrency strategy, Kafka event flow, ADR 직접 연결 설계 문서, evidence·orchestration 정본.
- 서브에이전트: 사용하지 않음. 단일 문서 변경 범위이며 단일 Dev 작성자가 일관된 ADR 사실성을 유지했습니다.
- Combined Verifier: pending. STANDARD 정책에 따른 독립 검토·focused verification이 필요합니다.
- GitHub Actions CI: pending.
