import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
  vus: 20,        // 가이드 기준
  duration: '30s',
};

export function setup() {
  const loginRes = http.post(
      `${BASE_URL}/api/auth/login`,
      JSON.stringify({ username: 'loadtest_user', password: 'Test1234!' }),
      { headers: { 'Content-Type': 'application/json' } }
  );
  check(loginRes, { '로그인 성공': (r) => r.status === 200 });
  return { accessToken: loginRes.json('data.accessToken') };
}

export default function (data) {
  const res = http.get(
      `${BASE_URL}/api/boards/posts?sort=comments&page=0&size=10`,
      { headers: { Authorization: `Bearer ${data.accessToken}` } }
  );
  check(res, { '200 응답': (r) => r.status === 200 });
  sleep(1);
}