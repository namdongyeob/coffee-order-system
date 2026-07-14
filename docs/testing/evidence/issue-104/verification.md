# 검증 로그

Attempt: 1
Head: e09452e0d05b883606af7e6ae6bb38a500c67914

| 날짜 | Issue | Level | 결과 | 검증 범위 | 명령/Evidence | 비고 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-14 | Issue #104 Entity 컨벤션 통일 | Level 0 | PASS | Entity 5종 getter/생성자 시그니처 동일성, 문서·헤더 주석 정합성 | 독립 Combined Verifier subagent 실행(`docs/testing/evidence/issue-104/manual-qa.md`) | API/런타임 동작 변경 없는 내부 구현 치환이라 Level 5/6 대상 아님(acceptance-criteria.md 참고). |
| 2026-07-14 | Issue #104 Entity 컨벤션 통일 | Level 1 | PASS | 컴파일(main/test 소스) | `./gradlew compileJava`, `./gradlew compileTestJava` | 전체 테스트(`./gradlew test`)는 이번 변경과 무관한 기존 로컬 환경 문제로 실행 불가. Level 1 전체 회귀 최종 판정은 GitHub Actions `quality-gates` CI로 확인(PR 링크 참고). |

## 로컬 Level 1/2/4 실행 불가 환경 제약

이 작업 디렉터리 경로(`...\코드컨벤션`)에 한글이 포함되어 있고, 이 Windows 머신의 "비유니코드 프로그램용 언어(시스템 로캘)"가 MS949로 설정되어 있습니다. Gradle 테스트 워커가 classpath를 전달하는 `@gradle-worker-classpath....txt` argfile 경로 해석이 이 조합에서 깨져, `MenuControllerTest`, `OutboxEventPublisherTest`를 포함한 모든 테스트 클래스가 `java.lang.ClassNotFoundException`으로 즉시 실패합니다(`Test process encountered an unexpected problem`). 재현·격리 절차:

- `git stash`로 원본 `main` HEAD 상태에서 `./gradlew test` 재실행 — 동일하게 전체 실패 재현.
- `./gradlew --stop` 후 daemon 재시작, `--no-daemon`, PowerShell `chcp 65001` + `-Dsun.jnu.encoding=UTF-8` 시도 — 모두 동일 실패. `sun.jnu.encoding`은 JVM 시작 시 OS 시스템 로캘로 고정되어 런타임 플래그로 바뀌지 않습니다.
- `build/classes/java/test/...MenuControllerTest.class` 등 컴파일 산출물 존재 확인 — 컴파일 자체는 정상.

따라서 이 Issue의 Level 2(Controller/API), Level 4(Kafka 관련 OutboxEvent) 로컬 PASS는 이 머신에서 관찰 불가능합니다. 사용자 승인에 따라 로컬 pre-push harness gate(`--no-verify`)를 이번 1건에 한해 우회했고, Level 1/2/4의 실질 근거는 PR head에 대한 GitHub Actions `quality-gates` CI 결과(원격 실행 환경은 이 로컬 한글 경로/로캘 제약이 없음)로 대체합니다. CI 결과는 merge 전 PR에서 확인합니다. 이 환경 문제 자체의 근본 수정(시스템 로캘 변경 등)은 이 Issue 범위 밖입니다.
