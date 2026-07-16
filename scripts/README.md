# 저장소 운영 스크립트

## Git 훅 설치

```powershell
python scripts/install_git_hooks.py
git config --local --get core.hooksPath
```

- `pre-commit`: `main`과 `master` 직접 커밋을 차단합니다.
- `pre-push`: branch 이름의 `issue-N`을 읽어 evidence, Issue별 `verification.md`, 변경 Markdown 링크를 검사합니다.
- Git 표준 옵션 `--no-verify`로 훅을 우회할 수 있지만, 긴급 상황에서만 사용하고 우회 이유와 재검증 결과를 PR에 남깁니다.

## 하네스 검사

```powershell
python -m unittest discover -s scripts/tests -p "test_*.py"
python scripts/harness_gate.py --issue 23 --base-ref origin/main --check-links --include-worktree
python scripts/harness_gate.py --links-only --base-ref origin/main
```

검사 항목은 Level 5/6 결정과 이유, Attempt 필수 섹션, Issue별 검증 정본, 변경 Markdown의 상대 링크입니다. 실패 항목이 있으면 구체적인 경로와 누락 필드를 출력하고 종료 코드 1을 반환합니다.

전역 검증 뷰는 커밋하지 않습니다. 필요할 때 `python scripts/rebuild_verification_log.py`로 표준 출력에 재현합니다.

## DLT 선택 재발행

`replay_dlt_message.ps1`는 `order.completed.DLT`의 단일 partition/offset만 재조회합니다. 승인자와 사유가 비어 있거나 형식이 맞지 않으면 PowerShell parameter validation에서 중단합니다.

```powershell
.\scripts\replay_dlt_message.ps1 -Partition 0 -Offset 12 -ApprovedBy operator-a -Reason "Redis recovered"
```

스크립트는 원본 topic·partition·offset header를 fail-closed로 확인하고 공통 `ranking_event_ledger`에 eventId와 fingerprint를 먼저 예약한 뒤 `REPUBLISHED`로 원본 topic에 발행합니다. payload와 key는 보존하지만 DLT·예외·stacktrace header는 복사하지 않으며, JSON type header와 내부 `DLT_REPLAY` source header를 추가합니다. 이미 `COMMITTED`인 같은 fingerprint는 consumer에서 no-op이고, 다른 fingerprint는 ledger가 fail-closed 합니다.
