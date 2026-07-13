// 점진적 일상 부하에서 주문 API 기준선을 관찰합니다.
import { createOrder, prepareSyntheticUsers, PROFILE, requireKnownProfile, summaryOptions, thresholds } from './lib/order-scenario.js';

const profiles = {
  safe: { maxVUs: 2, chargeRounds: 1, stages: [{ duration: '3s', target: 1 }, { duration: '8s', target: 2 }, { duration: '3s', target: 0 }] },
  heavy: { maxVUs: 25, chargeRounds: 4, stages: [{ duration: '30s', target: 5 }, { duration: '2m', target: 25 }, { duration: '30s', target: 0 }] },
};
const selected = requireKnownProfile(profiles);

export const options = {
  scenarios: {
    order_load: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: selected.stages,
      gracefulRampDown: '5s',
      gracefulStop: '5s',
      tags: { test_type: 'load', profile: PROFILE },
    },
  },
  thresholds: thresholds(1000),
  ...summaryOptions,
};

export function setup() {
  return prepareSyntheticUsers(selected.maxVUs, selected.chargeRounds);
}

export default function (data) {
  createOrder(data, 'order_load');
}
