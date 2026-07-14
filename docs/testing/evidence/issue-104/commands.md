# Commands

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `Read`/`Grep`로 5개 Entity, 3개 Controller, 6개 Service 확인 | Entity 스타일 불일치와 헤더 주석 누락 실태 파악 | `Menu.java`만 Lombok, 나머지 4개는 수동 작성. `MenuController.java`, `MenuService.java`만 헤더 주석 누락 확인. |
| `gh issue create --title "Entity getter/protected 생성자 컨벤션 문서화 및 통일" ...` | 컨벤션 문서화 요청 Issue 생성 | https://github.com/namdongyeob/coffee-order-system/issues/104 |
| `Edit`로 `docs/architecture/layered-design-policy.md`에 Entity 절 추가 | 컨벤션 문서화 | 반영 완료. |
| `Edit`로 `UserPoint.java`, `Order.java`, `ProcessedEvent.java`, `OutboxEvent.java`를 Lombok 패턴으로 치환 | Entity 스타일 통일 | 반영 완료. |
| `Edit`로 `MenuController.java`, `MenuService.java`에 헤더 주석 추가 | 헤더 주석 컨벤션 준수 | 반영 완료. |
| `./gradlew compileJava -q` | 컴파일 확인 | 성공(경고 1건, deprecated API 무관 항목). |
| `./gradlew test -q` | 전체 테스트 확인 | `ClassNotFoundException`으로 전체 실패. |
| `git stash && ./gradlew test -q` (원본 `main` HEAD 상태) | 실패가 이번 변경 탓인지 확인 | 원본 상태에서도 동일한 `ClassNotFoundException` 재현. 기존 환경 문제로 판정, `git stash pop`으로 변경 복원. |
| `./gradlew compileTestJava -q` | 테스트 코드가 변경된 getter 호출부와 호환되는지 확인 | 성공(에러 없음). |
| `git checkout -b claude/issue-104-entity-getter-protected-convention` | 하네스 브랜치 규칙(`issue-N` 포함)에 맞춰 작업 브랜치 생성 | 생성 완료. |
| `git add` + `git commit` (Refs #104) | 변경 커밋 | 커밋 `e09452e0d05b883606af7e6ae6bb38a500c67914`. |
| `git diff main HEAD -- <7개 파일> > 임시 diff 파일` | 독립 리뷰용 diff 추출 | `%TEMP%/issue-104.diff` 생성. |
| Agent(general-purpose, fresh)로 독립 Combined Verifier 실행 | diff의 getter/생성자 동일성, 도메인 메서드 보존, 범위 밖 변경 여부 검증 | PASS, 세부 근거는 `manual-qa.md` 참고. |
| `git push -u origin claude/issue-104-entity-getter-protected-convention` (1차 시도) | 원격 push | 로컬 pre-push hook(하네스 gate)이 evidence 파일 누락으로 차단. |
| `git push --no-verify` (evidence 파일 작성 후) | 원격 push | 성공. 사용자 승인 하에 evidence 파일 미비/로컬 환경 제약으로 이번 1건 한정 hook 우회. |
| `gh pr create` + `python scripts/harness_gate.py --issue 104 --pr-body-file ...` | PR #105 생성 | preflight도 Level 2/4 PASS 행 부재로 FAILED, PR은 사용자 승인으로 생성 진행. CI의 `quality-gates` job도 동일한 evidence gate에서 차단됨을 확인(`gh run watch`). |
| `wsl -e bash -lc "git clone ... && git checkout claude/issue-104-..."` | 한글 경로/MS949 로캘 제약이 없는 WSL Ubuntu에 같은 브랜치 클론 | 성공. WSL에 JDK 17, 21 모두 설치되어 있음을 확인(JDK 17 사용). |
| WSL: `./gradlew test --tests '*MenuControllerTest*'` | Level 2 Controller 테스트 실제 실행 | PASS(tests=2, failures=0, errors=0). |
| `git clone ... C:\coffee-verify` (비한글 경로, Docker Desktop 사용 가능) | Kafka Testcontainers가 필요한 테스트 실행 환경 확보(WSL은 Docker 미연동) | 성공. |
| `C:\coffee-verify`: `./gradlew.bat test --tests '*OutboxEventIntegrationTest*'` | Level 4 Kafka 통합 테스트 실제 실행 | PASS(tests=2, failures=0, errors=0, 실제 Kafka Testcontainers 사용). |
| `C:\coffee-verify`: `./gradlew.bat test` (전체) | Level 1 전체 회귀 실제 실행 | PASS(`build/test-results/test/*.xml` 27개 파일 집계: tests=76, failures=0, errors=0, skipped=0). |
| `verification.md`/`acceptance-criteria.md`/`manual-qa.md` 갱신 후 재커밋·재push | evidence를 실제 실행 결과로 정본화 | 완료. |
