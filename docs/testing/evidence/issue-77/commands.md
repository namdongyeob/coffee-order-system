# Issue #77 Commands

## 수정 전 RED와 순서 추적

| 명령 또는 근거 | 목적 | 결과 |
| --- | --- | --- |
| live Issue #77의 격리 실행 | 기존 RED 확인 | FAIL, 1 test failure, `No records found for topic`, 127.8초 |
| `.\gradlew.bat cleanTest test --tests "*RankingEventConsumerDltIntegrationTest.retriesTwiceThenPublishesFailedRecordToDlt" --no-daemon --max-workers=1 --info` | 수정 전 현재 순서 추적 | PASS, 109.033초. observer와 main listener assignment 전에 input send가 시작되고 producer ack와 main assignment/reset이 경합 |
| 같은 exact 명령에서 `--info` 제외 | 수정 전 flaky 재확인 | PASS, 91.929초. 기존 RED와 현재 PASS가 함께 있어 비결정적 timing 결함으로 분류 |

## 수정 후 Level 4

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `.\gradlew.bat cleanTest test --tests "*RankingEventConsumerDltIntegrationTest.retriesTwiceThenPublishesFailedRecordToDlt" --no-daemon --max-workers=1` | 격리 실행 1 | PASS, 91.755초 |
| 같은 명령의 별도 새 Gradle 프로세스 | 격리 실행 2 | PASS, 77.557초 |
| 같은 명령의 별도 새 Gradle 프로세스 | 격리 실행 3 | PASS, 73.945초 |
| `.\gradlew.bat cleanTest test --tests "*RankingEventConsumerDltIntegrationTest" --tests "*RankingEventConsumerKafkaRedisIntegrationTest" --tests "*OrderEventKafkaIntegrationTest" --no-daemon --max-workers=1` | 관련 retry, DLT, Kafka/Redis consumer와 producer 회귀 | PASS, 150.966초 |

각 독립 격리 실행 전 현재 worktree 관련 Java/Gradle 프로세스가 0개임을 확인했습니다.

## 전체 Level 1

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `.\gradlew.bat cleanTest test --no-daemon --max-workers=1` | 전체 회귀 | PASS, 187.834초, XML 51 tests, failures 0, errors 0, skipped 0 |

## Repository 검증

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `python scripts/harness_gate.py --issue 77 --branch codex/issue-77-dlt-flaky --base-ref origin/main --check-links --include-worktree` | Issue #77 repository gate | PASS |
| `git diff --check` | whitespace와 patch 정적 검사 | PASS. LF가 다음 Git 처리 때 CRLF로 바뀐다는 working-copy warning만 있었습니다. |
| `python scripts/harness_gate.py --issue 77 --pr-body-file C:\Users\user\Documents\coffee-order-system-issue-77-pr-body.md` | 한국어 PR body preflight | PASS, UTF-8 BOM 없음 확인 |

## 도구와 환경

- Codex CLI: `codex-cli 0.141.0`.
- 실행 환경: Windows PowerShell, unrestricted filesystem, approval disabled.
- Java/Gradle 테스트는 `--no-daemon --max-workers=1`로 순차 실행했습니다.
