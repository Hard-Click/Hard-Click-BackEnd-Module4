// =============================================================
// FLOWN 게시글 목록 병목 - k6 부하 테스트 스크립트
// 대상 병목: GET /api/posts (댓글수 정렬 = 상관 서브쿼리 핫패스)
//
// 사용법 (윤종호 course-list-load-test.js 와 동일한 방식):
//   최적화 전: k6 run -e BASE_URL=http://localhost:8080 -e STAGE=before -e SCENARIO=comments k6_post_list_load_test.js
//   최적화 후: k6 run -e BASE_URL=http://localhost:8080 -e STAGE=after  -e SCENARIO=comments k6_post_list_load_test.js
//
// 결과: k6-post-list-{STAGE}.md 파일로 자동 저장
// SLO 기준 (박종준): 게시글 조회 P95 200ms 이하, 에러율 1% 미만
// =============================================================

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL  || 'http://localhost:8080';
const STAGE    = __ENV.STAGE     || 'before';              // before | after
const SCENARIO = __ENV.SCENARIO  || 'comments';            // comments | latest | views
const BOARD    = __ENV.BOARD     || 'FREE';                // FREE | QUESTION
const PAGES    = parseInt(__ENV.PAGES || '5', 10);         // 0~PAGES-1 페이지 랜덤 조회
const USER     = __ENV.USER      || 'instructor1';
const PASS     = __ENV.PASS      || 'Test1234!';

// 커스텀 메트릭 — 기록표에 그대로 옮겨 적기 좋게 분리
const listLatency = new Trend('post_list_latency', true);
const listErrors  = new Rate('post_list_errors');

export const options = {
  scenarios: {
    ramping_load: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '20s', target: 20 },   // 워밍업
        { duration: '40s', target: 100 },  // 정상 부하
        { duration: '20s', target: 0 },    // 쿨다운
      ],
      exec: 'postListScenario',
      tags: { stage: STAGE },
    },
  },
  thresholds: {
    // SLO (박종준): 게시글 조회 P95 200ms 이하, 에러율 1% 미만
    [`post_list_latency{stage:${STAGE}}`]: ['p(95)<200'],
    'post_list_errors':                    ['rate<0.01'],
    'http_req_failed':                     ['rate<0.01'],
  },
  setupTimeout: '60s',
};

export function setup() {
  const loginRes = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({ username: USER, password: PASS }),
    { headers: { 'Content-Type': 'application/json' } }
  );

  if (!loginRes || loginRes.status === 0) {
    throw new Error(`서버에 연결할 수 없습니다 (${BASE_URL}). 서버가 실행 중인지 확인하세요.`);
  }

  if (loginRes.status !== 200) {
    throw new Error(`로그인 실패 (HTTP ${loginRes.status}). username/password를 확인하세요.\nbody: ${loginRes.body}`);
  }

  const accessToken = loginRes.json('data.accessToken');
  if (!accessToken) {
    throw new Error(`accessToken이 없습니다. 응답: ${loginRes.body}`);
  }

  return { accessToken };
}

export function postListScenario(data) {
  const page = Math.floor(Math.random() * PAGES);
  const url  = `${BASE_URL}/api/boards/${BOARD}/posts?sort=${SCENARIO}&page=${page}`;

  const res = http.get(url, {
    headers: { Authorization: `Bearer ${data.accessToken}` },
    tags: { name: 'GET /api/posts', stage: STAGE },
  });

  listLatency.add(res.timings.duration, { stage: STAGE });

  const ok = check(res, {
    '200 응답':    (r) => r.status === 200,
    'body exists': (r) => r.body && r.body.length > 0,
  });
  // SLO 레이턴시는 별도 표시 (에러율 오염 방지)
  check(res, { 'latency < 200ms (SLO)': (r) => r.timings.duration < 200 });
  listErrors.add(!ok); // HTTP 실패만 에러로 집계

  sleep(Math.random() * 0.5 + 0.1); // 0.1~0.6s think-time
}

export function handleSummary(data) {
  const m = data.metrics;
  const p = (name, key) => m[name]?.values?.[key] ?? 0;

  const p95  = p('post_list_latency', 'p(95)').toFixed(0);
  const p50  = p('post_list_latency', 'med').toFixed(0);
  const p90  = p('post_list_latency', 'p(90)').toFixed(0);
  const p99  = p('post_list_latency', 'p(99)').toFixed(0);
  const rps  = p('http_reqs', 'rate').toFixed(1);
  const reqs = p('http_reqs', 'count');
  const errs = (p('post_list_errors', 'rate') * 100).toFixed(2);

  const md = `# 게시글 목록 API 부하테스트 — ${STAGE.toUpperCase()} (SCENARIO=${SCENARIO})

| 지표 | 값 | SLO |
|---|---|---|
| 요청 수 | ${reqs} | - |
| Throughput | ${rps} req/s | - |
| 에러율 | ${errs}% | < 1% |
| P50 | ${p50}ms | - |
| P90 | ${p90}ms | - |
| P95 | ${p95}ms | **< 200ms** |
| P99 | ${p99}ms | - |
`;

  return {
    [`k6-post-list-${STAGE}.md`]: md,
    stdout: `\n================ k6 요약 (STAGE=${STAGE}, SCENARIO=${SCENARIO}) ================\n`
      + ` P95 latency : ${p95} ms   (SLO 목표 < 200ms)\n`
      + ` Throughput  : ${rps} req/s  (총 ${reqs} req)\n`
      + ` Error rate  : ${errs} %    (SLO 목표 < 1%)\n`
      + `==============================================================\n`
      + ` → 위 수치를 측정결과_기록표.xlsx 해당 방법 행에 입력\n`,
  };
}
