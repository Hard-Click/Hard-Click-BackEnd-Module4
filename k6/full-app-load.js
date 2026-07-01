import http from 'k6/http';
import { check, sleep } from 'k6';

// 목적: 결제 한 엔드포인트가 아니라 "전체 서비스"에 실제 사용자 흐름과 비슷한 부하를 걸어서,
//       Datadog APM Traces(Top List, group by resource_name)에서 어떤 API/SQL이
//       가장 큰 누적 부하(호출수 x 소요시간)를 차지하는지 찾기 위한 스크립트.
//
// 사용법:
//   1) 로그인 토큰을 curl로 미리 받아서 TOKEN으로 넘긴다.
//      (k6 setup()에서 직접 로그인하면 첫 요청이 원인 불명의 401을 반환하는 경우가 있어
//       curl로 미리 받은 토큰을 넘기는 방식으로 우회한다.)
//
//      TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
//        -H "Content-Type: application/json" \
//        -d '{"username":"demo_student","password":"Test1234!"}' \
//        | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
//      K6_WEB_DASHBOARD=true k6 run -e TOKEN="$TOKEN" -e VUS=30 -e DURATION=2m k6/full-app-load.js
//
// 옵션:
//   BASE_URL   서버 주소 (기본 http://localhost:8080)
//   TOKEN      미리 발급받은 accessToken (없으면 setup()에서 재시도하며 직접 로그인)
//   VUS        동시 가상 사용자 수 (기본 20)
//   DURATION   지속 시간 (기본 2m)
//   COURSE_ID  조회에 사용할 강의 ID (기본 1)
//   USERNAME / PASSWORD  로그인 계정 (기본 demo_student / Test1234!)

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const PRESET_TOKEN = __ENV.TOKEN || '';
const VUS = Number(__ENV.VUS || 20);
const DURATION = __ENV.DURATION || '2m';
const COURSE_ID = Number(__ENV.COURSE_ID || 1);
const POST_MAX_ID = Number(__ENV.POST_MAX_ID || 2000); // 시드된 게시글 최대 id
const USERNAME = __ENV.USERNAME || 'demo_student';
const PASSWORD = __ENV.PASSWORD || 'Test1234!';

export const options = {
    scenarios: {
        mixed_traffic: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '20s', target: VUS },   // 천천히 부하 올리기
                { duration: DURATION, target: VUS }, // 목표 부하 유지
                { duration: '10s', target: 0 },     // 정리
            ],
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<2000'],
    },
};

// 모든 VU가 공유하는 로그인 토큰을 한 번만 발급받는다.
// TOKEN이 미리 주어지면 그걸 그대로 쓰고, 없으면 재시도하며 직접 로그인한다.
export function setup() {
    if (PRESET_TOKEN) {
        return { token: PRESET_TOKEN };
    }

    for (let attempt = 1; attempt <= 3; attempt++) {
        const res = http.post(
            `${BASE_URL}/api/auth/login`,
            JSON.stringify({ username: USERNAME, password: PASSWORD }),
            { headers: { 'Content-Type': 'application/json' } }
        );

        if (res.status === 200) {
            const body = JSON.parse(res.body);
            const token = body.data && body.data.accessToken ? body.data.accessToken : '';
            return { token };
        }

        console.error(`로그인 실패 (attempt=${attempt}, status=${res.status}): ${res.body}`);
        sleep(1);
    }

    return { token: '' };
}

// 가중치 기반으로 실제 트래픽과 비슷한 비율로 엔드포인트를 섞는다.
// (목록 조회 > 상세 조회 > 리뷰/공지 > 마이페이지 순으로 흔히 호출됨)
const weightedScenarios = [
    { weight: 22, name: 'course-list', auth: false },
    { weight: 14, name: 'course-detail', auth: false },
    { weight: 12, name: 'course-reviews', auth: false },
    { weight: 18, name: 'post-list-comments', auth: true }, // 게시글 목록(댓글수 정렬, 상관 서브쿼리)
    { weight: 12, name: 'post-comments', auth: true },      // 댓글 목록(재귀 N+1)
    { weight: 6, name: 'subjects', auth: true },
    { weight: 6, name: 'notices', auth: true },
    { weight: 5, name: 'my-courses', auth: true },
    { weight: 5, name: 'my-payments', auth: true },
];

function pickScenario() {
    const total = weightedScenarios.reduce((sum, s) => sum + s.weight, 0);
    let r = Math.random() * total;
    for (const s of weightedScenarios) {
        if (r < s.weight) return s;
        r -= s.weight;
    }
    return weightedScenarios[0];
}

export default function (data) {
    const token = data.token;
    const authHeaders = token ? { Authorization: `Bearer ${token}` } : {};
    const scenario = pickScenario();

    let res;
    switch (scenario.name) {
        case 'course-list':
            res = http.get(`${BASE_URL}/api/courses?page=0&size=12&sort=POPULAR`);
            break;

        case 'course-detail':
            res = http.get(`${BASE_URL}/api/courses/${COURSE_ID}`);
            break;

        case 'course-reviews':
            res = http.get(`${BASE_URL}/api/courses/${COURSE_ID}/reviews?page=0`);
            break;

        case 'subjects':
            res = http.get(`${BASE_URL}/api/subjects`, { headers: authHeaders });
            break;

        case 'post-list-comments':
            // 댓글수 정렬 → 상관 서브쿼리(SELECT COUNT ... WHERE post_id=p.id)가 행마다 실행됨
            res = http.get(`${BASE_URL}/api/boards/posts?sort=comments&page=0`, {
                headers: authHeaders,
            });
            break;

        case 'post-comments': {
            // 댓글 목록 → 재귀 N+1 (원댓글마다 작성자명+답글 개별 조회)
            const postId = 1 + Math.floor(Math.random() * POST_MAX_ID);
            res = http.get(`${BASE_URL}/api/posts/${postId}/comments`, {
                headers: authHeaders,
            });
            break;
        }

        case 'notices':
            res = http.get(`${BASE_URL}/api/notices?type=GLOBAL&page=0&size=10`, {
                headers: authHeaders,
            });
            break;

        case 'my-courses':
            res = http.get(`${BASE_URL}/api/members/me/courses`, { headers: authHeaders });
            break;

        case 'my-payments':
            res = http.get(`${BASE_URL}/api/payment/me?page=0&size=10`, { headers: authHeaders });
            break;
    }

    check(res, {
        [`${scenario.name}: 2xx/4xx (서버 에러 없음)`]: (r) => r.status < 500,
    });

    sleep(Math.random() * 1.5 + 0.5); // 0.5~2초 사이 랜덤 think time (실제 유저 흉내)
}

// 결과 해석 가이드:
//   1) k6 실행하는 동안 Datadog → APM → Traces 에서
//        service:hardclick-api, Visualize as: Top List, Group by: resource_name
//      으로 보면서 count x avg duration(=누적 점유시간)이 가장 큰 resource를 찾는다.
//   2) 1위로 나온 resource를 클릭해 flame graph로 들어가
//      Controller/Service/Repository/SQL 중 어느 구간이 긴지 확인한다.
//   3) Grafana 쪽에서 동시간대 DB CPU/커넥션풀/Redis 지표도 같이 봐서
//      코드 문제(N+1 등)인지 인프라 사이징 문제인지 구분한다.
