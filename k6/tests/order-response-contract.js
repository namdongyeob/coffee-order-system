// 주문 응답 JSON parse 실패가 성공으로 분류되지 않는 계약을 실제 k6 runtime에서 검증합니다.
import { check } from 'k6';
import { Rate } from 'k6/metrics';
import { classifyOrderResponse, createOrder } from '../lib/order-scenario.js';

const contractAssertions = new Rate('contract_assertions');

export const options = {
  vus: 1,
  iterations: 1,
  thresholds: { contract_assertions: ['rate==1'] },
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

function recorder() {
  const values = [];
  return { values, add: (value) => values.push(value) };
}

function runCreateOrder(response) {
  const successRate = recorder();
  const errorRate = recorder();
  createOrder(
    { userBase: 130000, menuId: 1 },
    'order_contract',
    {
      post: () => response,
      successRate,
      errorRate,
      sleep: () => {},
    },
  );
  return { success: successRate.values, error: errorRate.values };
}

export default function () {
  const valid = classifyOrderResponse(validResponse);
  const malformed = classifyOrderResponse(malformedJsonResponse);
  const validMetrics = runCreateOrder(validResponse);
  const malformedMetrics = runCreateOrder(malformedJsonResponse);
  const missingFieldMetrics = runCreateOrder(missingFieldResponse);
  const badStatusMetrics = runCreateOrder({ ...validResponse, status: 500 });
  const nonJsonMetrics = runCreateOrder({
    ...validResponse,
    headers: { 'Content-Type': 'text/plain' },
  });

  const assertions = {
    'valid order response succeeds': () => valid.succeeded,
    'malformed JSON is classified as an error without escaping': () =>
      !malformed.succeeded && !malformed.bodyOk,
    'createOrder records valid JSON as success only': () =>
      validMetrics.success.length === 1 && validMetrics.success[0] === true &&
      validMetrics.error.length === 1 && validMetrics.error[0] === false,
    'createOrder records malformed JSON as error only': () =>
      malformedMetrics.success.length === 1 && malformedMetrics.success[0] === false &&
      malformedMetrics.error.length === 1 && malformedMetrics.error[0] === true,
    'createOrder records missing required fields as error only': () =>
      missingFieldMetrics.success.length === 1 && missingFieldMetrics.success[0] === false &&
      missingFieldMetrics.error.length === 1 && missingFieldMetrics.error[0] === true,
    'createOrder records bad status as error only': () =>
      badStatusMetrics.success.length === 1 && badStatusMetrics.success[0] === false &&
      badStatusMetrics.error.length === 1 && badStatusMetrics.error[0] === true,
    'createOrder records non JSON as error only': () =>
      nonJsonMetrics.success.length === 1 && nonJsonMetrics.success[0] === false &&
      nonJsonMetrics.error.length === 1 && nonJsonMetrics.error[0] === true,
  };
  check(null, assertions);
  Object.values(assertions).forEach((assertion) => contractAssertions.add(assertion()));
}
