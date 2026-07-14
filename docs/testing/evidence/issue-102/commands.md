# Commands

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `gh api repos/usersy628/coffee-order-system/contents/README.md` | 참고 스타일 README 확인 | 1001줄, 설계 의도·기술 선택 표·정책·API 명세 구조 확인. 단 해당 저장소는 미구현 상태(열린 Issue 10개가 전부 기능 구현 대기)임을 발견. |
| `gh issue list --repo usersy628/coffee-order-system --state all`, `gh api .../commits` | 참고 저장소 실제 구현 상태 확인 | 닫힌 Issue 2개(기술스택 문서화, 프로젝트 부트스트랩)뿐, 필수 API 전부 미구현 확인. README 내용을 그대로 베끼면 안 되는 근거로 사용. |
| `Read docs/product/requirements.md`, `scope.md`, `domain/*.md`, `api/api-spec.md`, `db/erd.md`, `architecture/overview.md` | 실제 정책·설계 문서 확인 | README에 압축 반영할 사실 근거 확보. |
| `cat src/main/resources/db/migration/*.sql` | 실제 스키마 확인 | V1~V5 파일명·목적을 README 마이그레이션 서술과 일치시킴. |
| `cat` 각 Controller·DTO(`MenuController`, `PointController`, `OrderController`, 관련 DTO) | 실제 API 요청/응답 형태 확인 | README API 예시가 실제 필드명·타입과 정확히 일치하도록 작성. |
| `cat src/main/java/.../common/ErrorCode.java` | 실제 오류 코드 확인 | README 오류 코드 표가 실제 6개와 정확히 일치하도록 작성. |
| `Read build.gradle` | 실제 의존성 버전 확인 | Spring Boot 4.1.0, Java 17, Redisson 4.6.1 등 기술 스택 표에 정확히 반영. |
| `find src/main/java/.../coffeeordersystem -maxdepth 2 -type d` | 실제 패키지 구조 확인 | README 패키지 트리를 실제 디렉터리와 일치시킴. |
| `cat docs/operations/local-runbook.md` | 실제 로컬 실행 절차 확인 | `docker/compose.yaml` 경로, 포트(13306/16379/19092/18080/15540), `bootRun` 명령을 정확히 반영. |
| `python scripts/harness_gate.py --links-only --base-ref main --include-worktree` | README 신규 링크 검증 | PASS. |
| 독립 검증 agent 실행(general-purpose, fresh) | README 사실 검증 | 9/10 항목 정확, 1건(테스트 개수) 지적 — agent 자신의 grep 오탐으로 판명. |
| `grep -rE "^\s*@Test(\(.*\))?\s*$" src/test --include=*.java \| wc -l` | agent 지적 사항 재검증 | 76(정확), agent가 사용한 substring grep(`@Test`)이 `@TestConfiguration`/`@Testcontainers`를 오카운트했음을 확인. README 수정 불필요로 결론. |
| `git add README.md && git commit` | 변경 커밋 | `Harness gate PASSED.` 커밋 `224b3232583f315348009a74ccf073ab8ac71e81`. |
