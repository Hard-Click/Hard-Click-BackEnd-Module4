import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Counter } from 'k6/metrics';

// 댓글 조회 N+1 최적화 Before/After 비교 부하테스트
//
// 측정 목표:
//   BEFORE: MemberNameAdapter.findAllById() → 작성자 수만큼 SELECT N+1
//   AFTER:  findByIdIn() → IN 쿼리 1회 + idx_comments_parent_id 인덱스
//
// 사용법:
//   # BEFORE (최적화 전 — develop 브랜치)
//   K6_WEB_DASHBOARD=true k6 run \
//     -e STUDENT_TOKEN=<jwt> \
//     -e POST_ID=<댓글_많은_게시글_id> \
//     --tag phase=before \
//     k6/comment-query-load-test.js
//
//   # AFTER (최적화 후 — PR 머지 & 서버 재시작)
//   K6_WEB_DASHBOARD=true k6 run \
//     -e STUDENT_TOKEN=<jwt> \
//     -e POST_ID=<same_id> \
//     --tag phase=after \
//     k6/comment-query-load-test.js

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const VUS      = Number(__ENV.VUS      || 20);
const DURATION = __ENV.DURATION        || '2m';
const POST_ID  = Number(__ENV.POST_ID  || 1);

const STUDENT_TOKEN = __ENV.STUDENT_TOKEN || '';

const commentDuration = new Trend('comment_query_duration', true);
const commentSuccess  = new Counter('comment_query_success');
const commentFail     = new Counter('comment_query_fail');

export const options = {
    scenarios: {
        comment_load: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '15s', target: VUS },
                { duration: DURATION, target: VUS },
                { duration: '10s', target: 0 },
            ],
        },
    },
    thresholds: {
        // Before: p95 수백ms 예상 / After: p95 < 50ms 목표
        'comment_query_duration': ['p(95)<500'],
        'http_req_failed':        ['rate<0.01'],
    },
};

export default function () {
    const headers = STUDENT_TOKEN
        ? { Authorization: `Bearer ${STUDENT_TOKEN}` }
        : {};

    const res = http.get(`${BASE_URL}/api/posts/${POST_ID}/comments`, { headers });

    commentDuration.add(res.timings.duration);

    const ok = check(res, {
        'status 200':             (r) => r.status === 200,
        'body has comments':      (r) => r.body && r.body.includes('commentId'),
        'response under 500ms':   (r) => r.timings.duration < 500,
    });

    if (ok) commentSuccess.add(1);
    else     commentFail.add(1);

    sleep(0.5);
}
