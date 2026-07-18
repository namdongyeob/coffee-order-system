# Issue #114 Manual QA

Issue: #114
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/114
Date: 2026-07-18

## 읽은 문서와 역할

- STRICT Dev/evidence writer 한 명이 runtime 검증과 evidence 작성의 유일한 writer였습니다. 구현 subagent는 사용하지 않았습니다.
- `AGENTS.md`, coffee-order issue loop, QA Gate, Context Router, orchestration policy, agent rules, test strategy와 evidence guide를 확인했습니다.
- 실행 계약으로 Docker README, local runbook, API spec과 k6 README를 추가 확인했습니다.
- 허용 범위는 Issue #114 runtime 검증과 `docs/testing/evidence/issue-114/**`이며 production/test/script 변경은 금지했습니다.

## Automated verification

- clean compose에서 MySQL 8.4.5, Redis 7.4.2, Kafka 3.9.1 health를 확인했습니다.
- 앱 PID 33492가 local profile, Flyway V7과 Tomcat 8080으로 기동하고 health HTTP 200 `UP`을 반환했습니다.
- k6 v2.0.0 safe Load·Stress·Spike를 순차 실행하고 각 5개 threshold와 summary JSON을 확인했습니다.
- final drain에서 DB 원천·Outbox·consumer 처리·ledger·Kafka offset·Redis 파생 score의 총수를 비교했습니다.

## Manual QA

- 메뉴는 HTTP 200으로 아메리카노·카페라떼·카푸치노·에스프레소 4개를 반환했습니다.
- user 114011에 10,000P 충전은 HTTP 200 balance 10000, menu 1 주문은 HTTP 201 order 1·PAID·4,500P였습니다.
- 주문 뒤 `orders`는 PAID 1건, `user_point` balance는 5,500, Outbox event `0a8b48c7-0e15-4fe6-bbb1-d8a13d4d7eb2`는 published 상태였습니다.
- 같은 event ID가 Kafka `order.completed`, `processed_event`, `ranking_event_ledger`의 `COMMITTED/NORMAL_CONSUMER`에 기록됐습니다.
- Redis `popular:menus:2026-07-18`은 member 1 score 1로 증가했고 인기 메뉴 API는 rank 1 아메리카노 orderCount 1을 반환했습니다.
- k6가 만든 synthetic 주문까지 drain한 뒤 orders·Outbox·processed·COMMITTED ledger는 각각 517건, unpublished 0, Kafka offset 517/517 lag 0, Redis member 1 score 517로 일치했습니다.

## Adversarial QA

- amount 0 충전은 HTTP 400 `INVALID_CHARGE_AMOUNT`를 반환했습니다.
- user 114012에 1P만 충전한 주문은 HTTP 409 `INSUFFICIENT_POINT`를 반환했고 DB balance는 1로 유지됐습니다.
- Windows `curl.exe` quoting으로 body가 손상된 POST 응답은 최종 근거에서 제외하고 새 synthetic user로 UTF-8 JSON 요청을 재실행했습니다.
- startup INFO `NOT_COORDINATOR`는 partition assignment 이후 재현되지 않았고 최종 Kafka lag 0, 앱 health UP, actual ERROR level·`Caused by`·stack frame 각 0건으로 확인했습니다.
- 과거 untracked k6 결과를 사용하지 않고 exact production head에서 새 summary 3개를 저장소 밖에 생성했습니다.

## Cleanup receipt

- 앱 PID 33492를 종료하자 Gradle wrapper PID 35160과 30768도 추가 강제 종료 없이 끝났습니다.
- `docker compose -f docker/compose.yaml down -v --remove-orphans`가 exit 0으로 이 검증의 MySQL·Redis·Kafka container와 network를 제거했습니다.
- cleanup 뒤 compose rows, project container, network, volume, 8080 listener와 worktree 앱 프로세스는 각각 0건이었습니다.
- 전체 Docker container도 0건이었고 다른 프로젝트 container나 사용자 파일은 변경하지 않았습니다.

## 미검증 항목과 남은 위험

- 로컬 전체 Gradle 회귀는 사용자 지시와 테스트 전략에 따라 실행하지 않았습니다. PR head `5aedd45dbc3d0fea25757ae13f18f0084853a653`의 GitHub Actions `quality-gates` run `29635241238`은 SUCCESS였습니다.
- fresh Review에서 전체 PASS를 막는 P1 2건과 metadata P2 1건을 발견했습니다. independent QA는 pending입니다.
- verified production head는 `e9412ab3cc4ceb56de5b4ae9659a0e9e3a5d59ec`입니다. evidence-only commit 뒤 PR head와의 차이는 이 evidence 디렉터리뿐이며 runtime 코드는 바뀌지 않습니다.
- 첫 명령 실행 전 과거 exited container가 이미 존재하지 않아 이전 실행의 `exit 255` 원인 로그는 회수할 수 없었습니다. Issue #114과 직접 연결 Issue의 본문·댓글, repository evidence와 Git history에도 동시대 로그·resource snapshot이 없습니다.
- 이번 clean 실행은 exit 255를 재현하지 않았고 세 container 모두 restart 0·exit 0·OOM false였습니다. 이 현재 관찰은 과거 `exit 255` 원인 확인을 대체하지 않으므로 Issue 판정은 BLOCKED입니다.
- Attempt 2는 evidence-only correction이며 새 runtime/k6 검증을 실행하지 않았습니다.
- 저장소 하네스는 Level 5/6 required YES에 PASS 행을 요구하면서 BLOCKED disposition에는 PASS 행을 금지합니다. 허용 범위에 harness script가 없어 preflight PASS를 만들 수 없고 PR body update는 fail-closed로 보류했습니다.
