// 짧은 급증과 회복 구간에서 주문 API의 실패율과 지연을 관찰합니다.
import { createOrder, prepareSyntheticUsers, PROFILE, requireKnownProfile, summaryOptions, thresholds } from './lib/order-scenario.js';

const profiles = {
  safe: { maxVUs: 8, chargeRounds: 1, stages: [{ duration: '3s', target: 1 }, { duration: '2s', target: 8 }, { duration: '5s', target: 8 }, { duration: '3s', target: 1 }, { duration: '3s', target: 0 }] },
  heavy: { maxVUs: 75, chargeRounds: 4, stages: [{ duration: '30s', target: 5 }, { duration: '10s', target: 75 }, { duration: '1m', target: 75 }, { duration: '30s', target: 5 }, { duration: '30s', target: 0 }] },
};
const selected = requireKnownProfile(profiles);

export const options = {
  scenarios: {
    order_spike: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: selected.stages,
      gracefulRampDown: '5s',
      gracefulStop: '5s',
      tags: { test_type: 'spike', profile: PROFILE },
    },
  },
  thresholds: thresholds(2000),
  ...summaryOptions,
};

export function setup() {
  return prepareSyntheticUsers(selected.maxVUs, selected.chargeRounds);
}

export default function (data) {
  createOrder(data, 'order_spike');
}
