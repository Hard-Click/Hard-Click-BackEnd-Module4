import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const SCENARIO = __ENV.SCENARIO || 'comments'; // comments | latest | views

export const options = {
  // 램프업 없이 VU 100 동시 시작, 1분 유지
  vus: 100,
  duration: '1m',
  // BEFORE처럼 느린 케이스에서 막 끝나가는 요청이 강제 중단(interrupted)되어
  // 결과가 왜곡되지 않도록 종료 대기 시간을 넉넉히 잡음
  gracefulStop: '30s',
  setupTimeout: '60s',
  thresholds: {
    // SLO-4-2 (유강현 기준): 게시글 조회 P95 1초 이하, 에러율 1% 미만
    http_req_duration: ['p(95)<1000'],
    http_req_failed: ['rate<0.01'],
  },
};

export function setup() {
  const loginRes = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({ username: 'bench_user_1', password: 'Test1234!' }),
    { headers: { 'Content-Type': 'application/json' } }
  );

  check(loginRes, { '로그인 성공': (r) => r.status === 200 });

  const accessToken = loginRes.json('data.accessToken');
  return { accessToken };
}

export default function (data) {
  const res = http.get(
    `${BASE_URL}/api/boards/FREE/posts?sort=${SCENARIO}&page=0`,
    { headers: { Authorization: `Bearer ${data.accessToken}` } }
  );

  check(res, { '200 응답': (r) => r.status === 200 });
  sleep(1);
}
