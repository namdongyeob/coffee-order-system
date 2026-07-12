# Issue #12 Attempt Log

Issue: #12
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/12
Branch: codex/issue-12-http-artifacts

## Attempt 1

### Generate

- 시작: 2026-07-12T20:09:16+09:00.
- 현재 Controller, DTO validation, 공통 에러 계약과 기존 HTTP 산출물을 대조했습니다.
- 성공·실패 API 요청을 고정 QA ID와 clean environment 순서로 구성했습니다.

### Evaluate

- FAIL: 첫 PowerShell `curl.exe -d` batch는 JSON quoting이 보존되지 않아 요청 body가 malformed 처리됐습니다.

### Failure Cause

- Windows PowerShell에서 inline JSON을 `curl.exe` 인자로 전달하는 quoting 방식이 요청 원문을 보존하지 못했습니다. API 또는 산출물 결함은 아닙니다.

### Change Scope

- `http/issue-12-api-validation.http`.
- Issue #12 evidence와 `docs/testing/verification-log.md`.

### Reverification

- clean volume으로 환경을 다시 만들고 저장소 밖 ASCII JSON 파일을 `--data-binary @file`로 전달하는 curl을 사용했습니다.

### Next Attempt

- 올바른 JSON 원문으로 성공·실패 계약과 cleanup을 재검증합니다.

## Attempt 2

### Generate

- clean project Compose와 local profile 애플리케이션을 다시 기동했습니다.
- 저장소 밖 임시 JSON body로 `.http` 산출물과 동일한 요청을 실행했습니다.

### Evaluate

- PASS: 메뉴 200, 충전 200, 주문 201, 인기 메뉴 200, 잔액 부족 409, 없는 메뉴 404를 확인했습니다.
- PASS: health 200 `UP`, local profile 앱 기동과 Kafka consumer partition assignment를 확인했습니다.
- PASS: 앱과 프로젝트 Compose를 종료하고 volume을 삭제했으며 기존 `rag-pgvector`만 남았습니다.

### Failure Cause

- 없음.

### Change Scope

- Issue #12 HTTP 산출물, evidence, verification-log만 유지합니다.

### Reverification

- 마지막 Reverification 종료 시각: 2026-07-12T20:18:12+09:00.
- 실제 요청·응답 원문과 cleanup 결과는 `manual-qa.md`에 기록했습니다.
- Issue #12 repository gate와 `git diff --check`가 PASS했습니다.

### Next Attempt

- 없음.
