// 안전한 기본 상한과 명시적 heavy 선택으로 주문 API의 성능 저하 구간을 탐색합니다.
import { createOrder, prepareSyntheticUsers, PROFILE, requireKnownProfile, summaryOptions, thresholds } from './lib/order-scenario.js';

const profiles = {
  safe: { maxVUs: 6, chargeRounds: 1, stages: [{ duration: '3s', target: 2 }, { duration: '5s', target: 4 }, { duration: '5s', target: 6 }, { duration: '3s', target: 0 }] },
  heavy: { maxVUs: 50, chargeRounds: 4, stages: [{ duration: '30s', target: 10 }, { duration: '1m', target: 25 }, { duration: '1m', target: 50 }, { duration: '30s', target: 0 }] },
};
const selected = requireKnownProfile(profiles);

export const options = {
  scenarios: {
    order_stress: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: selected.stages,
      gracefulRampDown: '5s',
      gracefulStop: '5s',
      tags: { test_type: 'stress', profile: PROFILE },
    },
  },
  thresholds: thresholds(1500),
  ...summaryOptions,
};

export function setup() {
  return prepareSyntheticUsers(selected.maxVUs, selected.chargeRounds);
}

export default function (data) {
  createOrder(data, 'order_stress');
}
