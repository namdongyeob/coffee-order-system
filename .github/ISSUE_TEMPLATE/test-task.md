---
name: 테스트 작업
about: 검증, 통합 테스트, Postman/http, k6 작업
title: "[test] "
labels: test
assignees: ""
---

## 목표

## 대상 검증 레벨

<!-- SOLO, STANDARD, STRICT 중 하나를 남기고 이유를 작성합니다. 단일 모듈 검증 기본값은 STANDARD이며 인프라·동시성·워크플로 변경이면 STRICT로 올립니다. -->
Execution mode: STANDARD
Execution mode reason: 단일 모듈 검증 작업으로, STRICT 위험 조건이 없다고 확인한 뒤 이 값을 유지합니다.

<!-- 각 required 값은 YES 또는 NO 하나만 남기고 이유를 작성합니다. 테스트 Issue의 보수적 기본값은 YES입니다. -->
Level 5 required: YES
Level 5 reason: 실제 애플리케이션 기동 검증이 불필요하다면 NO로 바꾸고 근거를 작성합니다.
Level 6 required: YES
Level 6 reason: 실제 HTTP 요청 검증이 불필요하다면 NO로 바꾸고 근거를 작성합니다.

## 시나리오

## 완료 기준

- [ ] `docs/ai/issue-completion-checklist.md`의 해당 공통 항목을 확인했습니다.
- [ ] 테스트 또는 스크립트가 있습니다.
- [ ] `docs/testing/evidence-guide.md`의 기본 evidence 파일을 작성했습니다.
- [ ] 누락된 환경 전제 조건이 문서화되었습니다.
