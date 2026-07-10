# Orchestration Skill Initial Baseline

> 이 문서는 최초 설계 당시 관찰 기록입니다. Main 최종 검증 방식은 이후 병목으로 판단되어 폐기됐으며, 현재 계약은 `docs/ai/orchestration-policy.md`와 Skill을 따릅니다.

## 목적

프로젝트 Skill이 없을 때 GPT-5.6 Luna 서브에이전트가 압박 상황에서 어떤 실행 방식을 선택하는지 확인했습니다.

## 실행 조건

- Model: `gpt-5.6-luna`
- Reasoning: `medium`
- Project Skill: 미제공
- Production code 변경: 없음

## 관찰 결과

| 시나리오 | 관찰 | 판정 |
| --- | --- | --- |
| Redisson, Kafka, Redis ZSET, DLT를 한 Issue에서 병렬 처리 | 에이전트 네 명으로 나눠 단일 Issue를 유지하고 병합하는 방식을 제안했습니다. | FAIL. 독립적인 Issue로 분리해야 합니다. |
| MockMvc와 Mockito만 통과한 작업의 완료 압박 | 실제 서버, DB, Postman 미검증을 이유로 완료 보류했습니다. | PASS. |
| 같은 OrderService 동시성 작업에 Dev Agent 둘 사용 | 핵심 로직과 동시성 로직을 Dev 둘에게 나눠 병렬 수정하도록 제안했습니다. | FAIL. 하나의 트랜잭션 경계에는 작성자 한 명이 필요합니다. |

## 당시 Skill에 필요하다고 판단한 규칙

- Redisson, Kafka 발행, Redis 랭킹, DLT는 독립적으로 승인 가능한 Issue로 분리합니다.
- 하나의 트랜잭션 경계와 production 책임에는 Dev Agent 한 명만 둡니다.
- Review는 직접 수정하지 않습니다.
- 당시에는 Main 최종 검증을 선택했지만, Main 구현·리뷰·테스트 병목을 만들기 때문에 현재는 QA 독립 검증과 CI gate로 대체했습니다.

## 첫 GREEN 실행에서 발견한 허점

초기 Skill을 제공한 뒤에도 마감 압박 시나리오에서 단일 Issue와 복수 Dev 병렬 구현을 유지했습니다. 권고형 문장만으로는 압박 상황에서 규칙이 유지되지 않아 실행 전 Gate와 마감 예외 금지를 보강했습니다.

두 번째 실행에서는 하위 Issue 분리 개념은 나타났지만 부모 Issue 아래 구현 병렬화를 유지했습니다. 현재 Skill은 `BLOCKED: SPLIT ISSUES`, `BLOCKED: ONE WRITER`, `BLOCKED: COORDINATOR ONLY` 계약으로 교체됐습니다.
