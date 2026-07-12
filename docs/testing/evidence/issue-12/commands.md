# Issue #12 Commands

## 선행 조건과 정적 확인

- `gh issue view 12 --json ...`: Issue #12 open과 Level 6 완료 기준을 확인했습니다.
- `gh pr view 1 --json ...`: PR #1은 merge commit `6feeed33bc7780441f8fe7dbd68b80ee30421451`로 MERGED입니다.
- Controller, request DTO, 공통 error handler와 API 명세를 읽어 현재 요청·응답 계약을 확인했습니다.

## Level 5 clean runtime

- `docker compose -f docker/compose.yaml down -v --remove-orphans`: 프로젝트 전용 상태를 정리했습니다.
- `docker compose -f docker/compose.yaml up -d --wait`: MySQL, Redis, Kafka healthy입니다.
- `.\gradlew.bat bootRun --args=--spring.profiles.active=local --no-daemon`: local profile 앱이 20.456초에 기동됐습니다.
- `curl.exe --noproxy '*' ... /actuator/health`: HTTP 200, `UP`입니다.

## Level 6 actual HTTP

- 첫 inline JSON curl batch는 PowerShell quoting 때문에 malformed body가 되어 400이었고 제품 결과로 사용하지 않았습니다.
- clean volume 재기동 뒤 저장소 밖 ASCII JSON body와 `curl.exe --data-binary @<temp-file>`로 `.http`와 동일한 요청을 실행했습니다.
- `GET /api/menus`: 200.
- `POST /api/points/charge` user 1201: 200, balance 10000.
- `POST /api/orders` user 1201/menu 1: 201, PAID 4500.
- `GET /api/menus/popular`: 두 번째 500ms poll에서 200, menu 1/orderCount 1.
- user 1202를 1000 충전 후 menu 1 주문: 409, `INSUFFICIENT_POINT`.
- user 1203을 10000 충전 후 menu 999999 주문: 404, `MENU_NOT_FOUND`.

## Cleanup

- 앱 process tree를 종료했습니다.
- `docker compose -f docker/compose.yaml down -v --remove-orphans`: 프로젝트 컨테이너·network·volume을 제거했습니다.
- `docker compose ... ps`: 비어 있습니다.
- port 8080 listener: `none`.
- `docker ps`: 기존 `rag-pgvector`만 남았고 건드리지 않았습니다.

## Repository verification

- `python scripts/harness_gate.py --issue 12 --branch codex/issue-12-http-artifacts --base-ref origin/main --check-links`: PASS.
- `git diff --check`: PASS.
- 저장소 밖 UTF-8 no-BOM 한국어 PR body를 `--pr-body-file`로 preflight한 뒤 동일 파일로 draft PR #74를 생성했습니다.
- `gh pr view 74`의 live body를 저장소 밖 파일로 다시 읽어 `--pr-body-file` preflight: PASS.
