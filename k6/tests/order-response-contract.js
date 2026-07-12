// 주문 응답 JSON parse 실패가 성공으로 분류되지 않는 계약을 실제 k6 runtime에서 검증합니다.
import { check } from 'k6';
import { classifyOrderResponse } from '../lib/order-scenario.js';

export const options = {
  vus: 1,
  iterations: 1,
  thresholds: { checks: ['rate==1'] },
};

const validResponse = {
  status: 201,
  headers: { 'Content-Type': 'application/json' },
  json: () => ({
    orderId: 1,
    userId: 130001,
    menuId: 1,
    menuName: 'synthetic-menu',
    paidAmount: 4500,
    status: 'PAID',
    orderedAt: '2026-07-12T21:00:00',
  }),
};

const malformedJsonResponse = {
  status: 201,
  headers: { 'Content-Type': 'application/json' },
  json: () => {
    throw new Error('malformed JSON');
  },
};

const missingFieldResponse = {
  status: 201,
  headers: { 'Content-Type': 'application/json' },
  json: () => ({ orderId: 1, status: 'PAID' }),
};

export default function () {
  const valid = classifyOrderResponse(validResponse);
  const malformed = classifyOrderResponse(malformedJsonResponse);
  const missingField = classifyOrderResponse(missingFieldResponse);

  check(null, {
    'valid order response succeeds': () => valid.succeeded,
    'malformed JSON is classified as an error without escaping': () =>
      !malformed.succeeded && !malformed.bodyOk,
    'missing required order fields are classified as an error': () =>
      !missingField.succeeded && !missingField.bodyOk,
  });
}
