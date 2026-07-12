// 주문 API k6 시나리오가 공유하는 synthetic 사용자 준비와 결과 분류 계약입니다.
import http from 'k6/http';
import { check, fail, sleep } from 'k6';
import { Rate } from 'k6/metrics';

export const orderSuccessRate = new Rate('order_success_rate');
export const orderErrorRate = new Rate('order_error_rate');

export const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
export const PROFILE = __ENV.K6_PROFILE || 'safe';
export const USER_BASE = Number(__ENV.K6_USER_BASE || '130000');
export const MENU_ID = Number(__ENV.K6_MENU_ID || '1');
export const CHARGE_AMOUNT = Number(__ENV.K6_CHARGE_AMOUNT || '1000000');
const THINK_TIME_SECONDS = Number(__ENV.K6_THINK_TIME_SECONDS || '0.2');

export function requireKnownProfile(profiles) {
  if (!Object.prototype.hasOwnProperty.call(profiles, PROFILE)) {
    throw new Error(`K6_PROFILE must be one of: ${Object.keys(profiles).join(', ')}`);
  }
  return profiles[PROFILE];
}

export function prepareSyntheticUsers(maxVUs, chargeRounds = 1) {
  for (let offset = 1; offset <= maxVUs; offset += 1) {
    const userId = USER_BASE + offset;
    for (let round = 0; round < chargeRounds; round += 1) {
      const response = http.post(
        `${BASE_URL}/api/points/charge`,
        JSON.stringify({ userId, amount: CHARGE_AMOUNT }),
        {
          headers: { 'Content-Type': 'application/json' },
          tags: { api: 'point_charge', data_class: 'synthetic' },
        },
      );

      const prepared = check(response, {
        'synthetic user charged': (result) => result.status === 200,
      });
      if (!prepared) {
        fail(`synthetic user ${userId} preparation failed with HTTP ${response.status}`);
      }
    }
  }

  return { userBase: USER_BASE, menuId: MENU_ID };
}

export function classifyOrderResponse(response) {
  const statusOk = response.status === 201;
  const contentType = (response.headers || {})['Content-Type'] ||
    (response.headers || {})['content-type'] || '';
  const contentTypeOk = contentType.includes('application/json');
  let bodyOk = false;

  if (contentTypeOk) {
    try {
      const body = response.json();
      bodyOk = body !== null && typeof body === 'object' &&
        typeof body.orderId === 'number' && body.orderId > 0 &&
        typeof body.userId === 'number' && body.userId > 0 &&
        typeof body.menuId === 'number' && body.menuId > 0 &&
        typeof body.menuName === 'string' && body.menuName.length > 0 &&
        typeof body.paidAmount === 'number' && body.paidAmount > 0 &&
        body.status === 'PAID' &&
        typeof body.orderedAt === 'string' && body.orderedAt.length > 0;
    } catch (_) {
      bodyOk = false;
    }
  }

  return {
    statusOk,
    contentTypeOk,
    bodyOk,
    succeeded: statusOk && contentTypeOk && bodyOk,
  };
}

export function createOrder(data, scenarioName) {
  const userId = data.userBase + __VU;
  const response = http.post(
    `${BASE_URL}/api/orders`,
    JSON.stringify({ userId, menuId: data.menuId }),
    {
      headers: { 'Content-Type': 'application/json' },
      tags: { api: 'orders', scenario: scenarioName, profile: PROFILE, data_class: 'synthetic' },
    },
  );

  const classification = classifyOrderResponse(response);
  check(classification, {
    'order returns 201': (result) => result.statusOk,
    'order response content type is JSON': (result) => result.contentTypeOk,
    'order response JSON has required fields': (result) => result.bodyOk,
  });
  orderSuccessRate.add(classification.succeeded);
  orderErrorRate.add(!classification.succeeded);
  sleep(THINK_TIME_SECONDS);
}

export function thresholds(p95Milliseconds) {
  return {
    checks: ['rate>0.99'],
    http_req_failed: ['rate<0.01'],
    http_req_duration: [`p(95)<${p95Milliseconds}`],
    order_success_rate: ['rate>0.99'],
    order_error_rate: ['rate<0.01'],
  };
}

export const summaryOptions = {
  setupTimeout: '2m',
  summaryTimeUnit: 'ms',
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'count'],
};
