import http from 'k6/http';
import { check, sleep } from 'k6';

// 사용법:
//   기본(실시간 웹 대시보드로 지켜보기 좋은 모드, 20초간 지속):
//     K6_WEB_DASHBOARD=true k6 run k6/duplicate-payment.js
//     → 실행 중 브라우저에서 http://127.0.0.1:5665 접속
//
//   한 번에 몰아서 100동시요청 (Before/After 비교용, 순간 부하):
//     K6_WEB_DASHBOARD=true k6 run -e SCENARIO=burst k6/duplicate-payment.js
//
// 옵션:
//   BASE_URL              결제 서버 주소 (기본 http://localhost:8080)
//   TOKEN                 Authorization Bearer 토큰
//   SAME_IDEMPOTENCY_KEY  true면 매 반복마다 "같은" 멱등키로 재요청 → 중복차단 시연
//   SCENARIO              live(기본, 지속형) | burst(순간 100동시요청형)
//   DURATION              live 모드 지속 시간 (기본 20s)
//   VUS                   live 모드 동시 사용자 수 (기본 20)

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const TOKEN = __ENV.TOKEN || '';
const COURSE_ID = Number(__ENV.COURSE_ID || 1);
const AMOUNT = Number(__ENV.AMOUNT || 10000);
const SAME_IDEMPOTENCY_KEY = (__ENV.SAME_IDEMPOTENCY_KEY || 'true') === 'true';
const SCENARIO = __ENV.SCENARIO || 'live';
const DURATION = __ENV.DURATION || '20s';
const VUS = Number(__ENV.VUS || 20);
// 동일 courseId에 대해 동시에 여러 명이 "결제하기"를 누르는 상황을 재현한다.
const FIXED_IDEMPOTENCY_KEY = __ENV.IDEMPOTENCY_KEY || 'duplicate-payment-test-key';

export const options = {
    scenarios:
        SCENARIO === 'burst'
            ? {
                  duplicate_click: {
                      executor: 'shared-iterations',
                      vus: 100,
                      iterations: 100,
                      maxDuration: '30s',
                  },
              }
            : {
                  duplicate_click: {
                      executor: 'constant-vus',
                      vus: VUS,
                      duration: DURATION,
                  },
              },
};

export default function () {
    const idempotencyKey = SAME_IDEMPOTENCY_KEY
        ? FIXED_IDEMPOTENCY_KEY
        : `${FIXED_IDEMPOTENCY_KEY}-${__VU}-${__ITER}`;

    const res = http.post(
        `${BASE_URL}/api/payments/confirm`,
        JSON.stringify({ courseId: COURSE_ID, amount: AMOUNT }),
        {
            headers: {
                'Content-Type': 'application/json',
                'Idempotency-Key': idempotencyKey,
                ...(TOKEN ? { Authorization: `Bearer ${TOKEN}` } : {}),
            },
        }
    );

    check(res, {
        '201(성공) 또는 409(중복차단) 또는 200(이미 처리됨) 중 하나': (r) =>
            r.status === 201 || r.status === 409 || r.status === 200,
    });

    if (SCENARIO !== 'burst') {
        sleep(1);
    }
}

// 결과 해석:
//   - 분산락+멱등키 적용 전(Before): 동일 idempotencyKey로 동시요청 시 여러 건이 201로 성공 → 중복결제
//   - 적용 후(After): 단 1건만 201, 나머지는 409(DUPLICATE_PAYMENT_REQUEST) 또는 200(이미 처리됨, duplicate=true)
//   - k6 summary의 http_req_duration / status code 분포를 Grafana 대시보드와 함께 캡처해 비교한다.
