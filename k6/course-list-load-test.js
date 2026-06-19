/**
 * 강의 목록 API (GET /api/courses) N+1 쿼리 최적화 — Before/After 부하테스트
 *
 * 가설: CourseQueryService.getList()가 페이지당 강의 1건마다
 *       리뷰 평점/리뷰수/수강생수 쿼리를 각각 따로 날려(N+1) size에 비례해 느려진다.
 *
 * 실행:
 *   최적화 전: k6 run -e STAGE=before k6/course-list-load-test.js
 *   최적화 후: k6 run -e STAGE=after  k6/course-list-load-test.js
 */
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const STAGE    = __ENV.STAGE    || 'before';
const PAGE_SIZE = Number(__ENV.PAGE_SIZE || 20);

const durationBySize = new Trend(`course_list_duration_size${PAGE_SIZE}`, true);

export const options = {
    scenarios: {
        course_list_ramp: {
            executor: 'ramping-vus',
            stages: [
                { duration: '20s', target: 10 },
                { duration: '40s', target: 30 },
                { duration: '20s', target: 0 },
            ],
            exec: 'courseListScenario',
            tags: { stage: STAGE },
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'],
        [`http_req_duration{stage:${STAGE}}`]: ['p(95)<1000'],
    },
};

export function courseListScenario() {
    const res = http.get(`${BASE_URL}/api/courses?page=0&size=${PAGE_SIZE}`, {
        tags: { type: 'course_list', stage: STAGE },
    });

    durationBySize.add(res.timings.duration);

    check(res, {
        '강의 목록 200': (r) => r.status === 200,
        '강의 목록 비어있지 않음': (r) => {
            try { return JSON.parse(r.body)?.data?.content?.length > 0; } catch { return false; }
        },
    });

    sleep(1);
}

export function handleSummary(data) {
    const m = data.metrics;
    const p = (name, key) => m[name]?.values?.[key] ?? 0;

    const md = `# 강의 목록 API 부하테스트 — ${STAGE.toUpperCase()} (page size=${PAGE_SIZE})

| 지표 | 값 |
|---|---|
| 요청 수 | ${p('http_reqs', 'count')} |
| 실패율 | ${(p('http_req_failed', 'rate') * 100).toFixed(2)}% |
| P50 | ${p('http_req_duration', 'med').toFixed(0)}ms |
| P90 | ${p('http_req_duration', 'p(90)').toFixed(0)}ms |
| P95 | ${p('http_req_duration', 'p(95)').toFixed(0)}ms |
| P99 | ${p('http_req_duration', 'p(99)').toFixed(0)}ms |
| MAX | ${p('http_req_duration', 'max').toFixed(0)}ms |
`;

    return {
        [`k6-course-list-${STAGE}-size${PAGE_SIZE}.md`]: md,
        stdout: md,
    };
}
