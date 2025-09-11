import http from 'k6/http';
import { check } from 'k6';
import { Counter, Trend } from 'k6/metrics';

// ---- 설정값
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// ---- 메트릭
const mResp = new Trend('coupon_response_time');

// ---- 피크 시나리오 (로컬 환경용)
export const options = {
  discardResponseBodies: true,
  scenarios: {
    peak: {
      executor: 'ramping-arrival-rate',
      startRate: 0,
      timeUnit: '1s',
      preAllocatedVUs: 400,
      maxVUs: 1500,
      stages: [
        { target: 1000 ,duration: '2s' },   // 피크 도달
        { target: 1000, duration: '30s' },  // 피크 유지
        { target: 0 , duration: '2s' },     // 램프 다운
      ],
      gracefulStop: '5s',
      tags: { test_type: 'peak' },
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.2'],
    'http_req_duration{scenario:peak}': ['p(95)<1000'],
  },
};

// ---- VU 함수
export default function () {
  const policyId = 3;

  // 유저 ID 샘플링
  const userId = Math.floor(Math.random() * 1000) + 1;

  // 요청
  const res = http.post(
    `${BASE_URL}/coupons/${policyId}`,
    JSON.stringify(userId),
    { headers: { 'Content-Type': 'application/json' } }
  );

  // 응답 시간 기록
  mResp.add(res.timings.duration);
}

