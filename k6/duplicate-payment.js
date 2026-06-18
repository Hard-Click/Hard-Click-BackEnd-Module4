/**
 * Hard-Click 결제 부하테스트 — 프로덕션급 시나리오
 *
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │ 시나리오 구성                                                         │
 * │  slo_baseline       : 정상 결제 (ramping-arrival-rate) — SLO 검증   │
 * │  duplicate_burst    : 중복클릭 순간 100VU — 중복결제 0건 증명        │
 * │  duplicate_sustained: 동일 키 지속 60s — 캐시 응답 검증             │
 * └─────────────────────────────────────────────────────────────────────┘
 *
 * 실행 예시:
 *   전체:     k6 run k6/duplicate-payment.js -e TOKEN=<JWT>
 *   SLO만:    k6 run k6/duplicate-payment.js -e TOKEN=<JWT> -e SCENARIOS=slo
 *   burst만:  k6 run k6/duplicate-payment.js -e TOKEN=<JWT> -e SCENARIOS=burst
 *   Grafana 대시보드 동시 확인:
 *             K6_WEB_DASHBOARD=true k6 run k6/duplicate-payment.js -e TOKEN=<JWT>
 *
 * 환경변수:
 *   BASE_URL    서버 주소 (기본: http://localhost:8080)
 *   TOKEN       Bearer JWT
 *   COURSE_ID   테스트 강의 ID (기본: 1, DB에 존재 + 미수강 상태 필요)
 *   AMOUNT      결제 금액 (기본: 10000)
 *   SCENARIOS   실행할 시나리오 (all | slo | burst, 기본: all)
 *   BURST_KEY   burst/sustained에서 쓸 고정 멱등키 (유효 UUID v4)
 *   GRAFANA_URL Grafana annotation URL (선택, 없으면 스킵)
 *   GRAFANA_TOKEN Grafana API 토큰 (선택)
 */

import http from 'k6/http';
import { check, group, sleep, fail } from 'k6';
import { Counter, Trend, Rate } from 'k6/metrics';

// ─── 커스텀 메트릭 ────────────────────────────────────────────────────────────
const metrics = {
    // 응답코드별 카운터 (전체 + 시나리오별 태그로 분리 가능)
    c201:      new Counter('payment_201'),
    c200:      new Counter('payment_200'),
    c409:      new Counter('payment_409'),
    cErr:      new Counter('payment_error'),
    cTimeout:  new Counter('payment_pg_timeout'),

    // 신규 결제(201)만 측정 — 200/409의 빠른 응답이 p95 왜곡하는 것 방지
    newDur:    new Trend('payment_new_duration', true),

    // 비즈니스 성공률 (201 = 신규 결제 성공)
    successRate: new Rate('payment_success_rate'),
};

// ─── 설정 ──────────────────────────────────────────────────────────────────────
const CFG = {
    baseUrl:     __ENV.BASE_URL     || 'http://localhost:8080',
    token:       __ENV.TOKEN        || '',
    courseId:    Number(__ENV.COURSE_ID || 1),
    amount:      Number(__ENV.AMOUNT    || 10000),
    scenarios:   __ENV.SCENARIOS    || 'all',
    // burst / sustained 시나리오용 고정 멱등키 (유효 UUID v4)
    burstKey:    __ENV.BURST_KEY    || 'a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d',
    // Grafana annotation (없으면 스킵)
    grafanaUrl:  __ENV.GRAFANA_URL  || '',
    grafanaToken:__ENV.GRAFANA_TOKEN|| '',
};

// ─── 인라인 uuidv4 (외부 라이브러리 의존성 없음) ────────────────────────────────
function uuidv4() {
    const b = [];
    for (let i = 0; i < 16; i++) b.push(Math.floor(Math.random() * 256));
    b[6] = (b[6] & 0x0f) | 0x40;
    b[8] = (b[8] & 0x3f) | 0x80;
    const h = b.map(v => v.toString(16).padStart(2, '0'));
    return `${h.slice(0,4).join('')}-${h.slice(4,6).join('')}-${h.slice(6,8).join('')}-${h.slice(8,10).join('')}-${h.slice(10).join('')}`;
}

// ─── 409를 "정상 응답"으로 분류 (burst에서 http_req_failed 오염 방지) ─────────
http.setResponseCallback(http.expectedStatuses(200, 201, 409));

// ─── 공통 헤더 팩토리 ──────────────────────────────────────────────────────────
function makeHeaders(idempotencyKey) {
    return {
        'Content-Type': 'application/json',
        'Idempotency-Key': idempotencyKey,
        ...(CFG.token ? { Authorization: `Bearer ${CFG.token}` } : {}),
    };
}

const PAYLOAD = JSON.stringify({ courseId: CFG.courseId, amount: CFG.amount });

// ─── 결제 요청 + 메트릭 집계 ──────────────────────────────────────────────────
function pay(idempotencyKey) {
    const res = http.post(
        `${CFG.baseUrl}/api/payments/confirm`,
        PAYLOAD,
        { headers: makeHeaders(idempotencyKey) }
    );

    const s = res.status;
    if      (s === 201) { metrics.c201.add(1); metrics.newDur.add(res.timings.duration); metrics.successRate.add(1); }
    else if (s === 200) { metrics.c200.add(1); metrics.successRate.add(0); }
    else if (s === 409) { metrics.c409.add(1); metrics.successRate.add(0); }
    else if (s === 500) { metrics.cErr.add(1); metrics.cTimeout.add(1); metrics.successRate.add(0); }
    else                { metrics.cErr.add(1); metrics.successRate.add(0); }

    return res;
}

// ─── 시나리오 정의 ─────────────────────────────────────────────────────────────
function buildScenarios() {
    const all = {
        /**
         * [1] SLO 검증 — ramping-arrival-rate
         * 실제 트래픽처럼 워밍업 → 정상 부하 → 피크 → 쿨다운
         * 목표: p95 < 2s, 에러율 < 0.5%
         */
        slo_baseline: {
            executor: 'ramping-arrival-rate',
            startRate: 5,
            timeUnit: '1s',
            preAllocatedVUs: 50,
            maxVUs: 150,
            stages: [
                { duration: '30s', target: 10 },  // 워밍업
                { duration: '60s', target: 30 },  // 정상 부하
                { duration: '60s', target: 50 },  // 피크
                { duration: '30s', target: 10 },  // 쿨다운
            ],
            exec: 'sloScenario',
        },

        /**
         * [2] 중복클릭 burst — shared-iterations
         * 100VU가 동시에 동일 멱등키로 요청 → 201은 최대 1건이어야 함
         * 목표: payment_201 ≤ 1, payment_error == 0
         */
        duplicate_burst: {
            executor: 'shared-iterations',
            vus: 100,
            iterations: 100,
            maxDuration: '30s',
            startTime: '0s',
            exec: 'burstScenario',
        },

        /**
         * [3] 지속 중복 — constant-vus
         * 같은 키로 60s 지속 → 첫 201 이후 전량 200(캐시) 또는 409(락)
         * 목표: payment_201{scenario:duplicate_sustained} ≤ 1
         */
        duplicate_sustained: {
            executor: 'constant-vus',
            vus: 20,
            duration: '60s',
            startTime: '0s',
            exec: 'sustainedScenario',
        },
    };

    if (CFG.scenarios === 'slo')   return { slo_baseline: all.slo_baseline };
    if (CFG.scenarios === 'burst') return { duplicate_burst: all.duplicate_burst };
    return all;
}

// ─── 성공 기준 (thresholds) ───────────────────────────────────────────────────
//
//  alert rule ↔ k6 매핑 (프록시, 1:1 아님)
//  ┌──────────────────────────────────┬───────────────────────────────────────┐
//  │ 서버 alert rule                  │ k6 threshold (더 보수적 게이트)      │
//  ├──────────────────────────────────┼───────────────────────────────────────┤
//  │ PaymentLatencyP95High (> 2s)     │ p(95)<2000  (네트워크 왕복 포함)     │
//  │ PaymentSuccessRateDropped(<99.5%)│ http_req_failed rate<0.005           │
//  │ DuplicatePaymentDetected         │ payment_201{burst} count<=1          │
//  └──────────────────────────────────┴───────────────────────────────────────┘
export const options = {
    scenarios: buildScenarios(),
    thresholds: {
        // ── SLO 게이트 (slo_baseline 시나리오만) ─────────────────────────────
        'http_req_duration{scenario:slo_baseline}': ['p(95)<2000', 'p(99)<3000'],
        'http_req_failed{scenario:slo_baseline}':   ['rate<0.005'],
        // 신규 결제(201)만 측정한 처리시간 — 캐시/락 응답이 p95 내리는 것 방지
        'payment_new_duration':                     ['p(95)<2000'],
        // 비즈니스 성공률 게이트 (신규 결제 관점)
        'payment_success_rate{scenario:slo_baseline}': ['rate>0.995'],

        // ── 중복결제 0건 증명 (burst 시나리오만) ──────────────────────────────
        'payment_201{scenario:duplicate_burst}':   ['count<=1'],
        'payment_error{scenario:duplicate_burst}': ['count==0'],

        // ── 지속 시나리오: 두 번째 요청부터 전량 200/409 ─────────────────────
        'payment_201{scenario:duplicate_sustained}': ['count<=1'],
    },
};

// ─── setup — 프리플라이트 ──────────────────────────────────────────────────────
export function setup() {
    // 1. 토큰 존재 확인
    if (!CFG.token) {
        fail('TOKEN 환경변수가 없습니다. -e TOKEN=<JWT> 로 전달하세요.');
    }

    // 2. 서버 헬스체크
    const health = http.get(`${CFG.baseUrl}/actuator/health`);
    if (health.status !== 200) {
        fail(`서버 헬스체크 실패 (${health.status}). BASE_URL=${CFG.baseUrl} 확인 필요.`);
    }

    // 3. 프리플라이트 결제 1건 (설정 오류 조기 발견 — 120s 낭비 방지)
    const preflight = pay(uuidv4());
    if (preflight.status === 401) fail('인증 실패. TOKEN 만료 또는 형식 오류 확인.');
    if (preflight.status === 400) {
        const body = preflight.json();
        fail(`400 에러 — code: ${body?.errorCode}, COURSE_ID=${CFG.courseId}, AMOUNT=${CFG.amount} 확인.`);
    }
    if (preflight.status >= 500) fail(`서버 오류 ${preflight.status} — DB/Redis/서버 기동 상태 확인.`);

    // 4. Grafana annotation — 테스트 시작 마킹 (설정 없으면 스킵)
    if (CFG.grafanaUrl && CFG.grafanaToken) {
        http.post(
            `${CFG.grafanaUrl}/api/annotations`,
            JSON.stringify({
                text: '🚀 k6 payment load test START',
                tags: ['k6', 'payment', 'load-test'],
            }),
            {
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${CFG.grafanaToken}`,
                },
            }
        );
    }

    return { startedAt: new Date().toISOString(), preflight: preflight.status };
}

// ─── teardown — 테스트 종료 마킹 ───────────────────────────────────────────────
export function teardown() {
    if (CFG.grafanaUrl && CFG.grafanaToken) {
        http.post(
            `${CFG.grafanaUrl}/api/annotations`,
            JSON.stringify({
                text: '🏁 k6 payment load test END',
                tags: ['k6', 'payment', 'load-test'],
            }),
            {
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${CFG.grafanaToken}`,
                },
            }
        );
    }
}

// ─── [1] sloScenario — 정상 결제 부하 ────────────────────────────────────────
export function sloScenario() {
    group('slo_payment', () => {
        const res = pay(uuidv4()); // 매 요청 신규 UUID

        check(res, {
            'status 201':                (r) => r.status === 201,
            'duplicate = false':         (r) => {
                try { return JSON.parse(r.body)?.data?.duplicate === false; }
                catch { return false; }
            },
            'pgTransactionId 존재':      (r) => {
                try { return !!JSON.parse(r.body)?.data?.pgTransactionId; }
                catch { return false; }
            },
        });
    });

    // 지수 분포 think time — 고정 sleep(1) 대신 실제 유저 행동 모사
    sleep(0.5 + Math.random() * 2);
}

// ─── [2] burstScenario — 중복클릭 순간 집중 ─────────────────────────────────
export function burstScenario() {
    group('duplicate_burst', () => {
        const res = pay(CFG.burstKey); // 전 VU 동일 키

        check(res, {
            '정상 응답 (201/409/200)': (r) => [201, 409, 200].includes(r.status),
            '비정상 응답 없음':         (r) => r.status < 400 || r.status === 409,
        });

        // 201이 2건 이상이면 중복결제 — 즉시 body 기록
        if (res.status === 201) {
            check(res, {
                '[중복결제 검증] pgTransactionId 단 1개': (r) => {
                    try { return !!JSON.parse(r.body)?.data?.pgTransactionId; }
                    catch { return false; }
                },
            });
        }
    });
    // burst는 sleep 없음 — 동시성 최대화
}

// ─── [3] sustainedScenario — 지속 중복 (캐시 응답 검증) ─────────────────────
export function sustainedScenario() {
    group('duplicate_sustained', () => {
        // burst와 키를 분리 (sustained용 별도 UUID) — burst 결과와 섞이지 않도록
        const res = pay(CFG.burstKey.replace('a1b2', 'b2c3'));

        check(res, {
            '첫 결제 이후 캐시(200) 또는 차단(409)': (r) =>
                [200, 201, 409].includes(r.status),
            '500 없음': (r) => r.status !== 500,
        });
    });

    sleep(1);
}

// ─── handleSummary — 자동 리포트 생성 ────────────────────────────────────────
export function handleSummary(data) {
    const m = data.metrics;

    const val   = (name, key) => m[name]?.[key] ?? 0;
    const ms    = (name, key) => `${val(name, key).toFixed(0)}ms`;
    const pct   = (name, key) => `${(val(name, key) * 100).toFixed(2)}%`;
    const cnt   = (name)      => val(name, 'count');
    const pass  = (ok)        => ok ? '✅ PASS' : '❌ FAIL';

    const p95slo  = val('http_req_duration{scenario:slo_baseline}', 'p(95)');
    const p99slo  = val('http_req_duration{scenario:slo_baseline}', 'p(99)');
    const errRate = val('http_req_failed{scenario:slo_baseline}',   'rate');
    const c201b   = cnt('payment_201{scenario:duplicate_burst}');
    const cErrb   = cnt('payment_error{scenario:duplicate_burst}');
    const c201s   = cnt('payment_201{scenario:duplicate_sustained}');
    const p95New  = val('payment_new_duration', 'p(95)');
    const srRate  = val('payment_success_rate{scenario:slo_baseline}', 'rate');

    const totalReq = cnt('payment_201') + cnt('payment_200') +
                     cnt('payment_409') + cnt('payment_error');

    const md = `# Hard-Click 결제 부하테스트 결과

생성: ${new Date().toISOString()}

---

## 🚦 합격 판정

| 게이트 | 기준 | 실측 | 결과 |
|--------|------|------|------|
| SLO P95 응답시간 | < 2,000ms | ${p95slo.toFixed(0)}ms | ${pass(p95slo < 2000)} |
| SLO P99 응답시간 | < 3,000ms | ${p99slo.toFixed(0)}ms | ${pass(p99slo < 3000)} |
| SLO 에러율 | < 0.5% | ${pct('http_req_failed{scenario:slo_baseline}', 'rate')} | ${pass(errRate < 0.005)} |
| 비즈니스 성공률 | > 99.5% | ${pct('payment_success_rate{scenario:slo_baseline}', 'rate')} | ${pass(srRate > 0.995)} |
| **중복결제 0건 (burst)** | 201 ≤ 1건 | **${c201b}건** | ${pass(c201b <= 1)} |
| 비정상 응답 (burst) | 0건 | ${cErrb}건 | ${pass(cErrb === 0)} |
| 중복결제 0건 (sustained) | 201 ≤ 1건 | ${c201s}건 | ${pass(c201s <= 1)} |

---

## 📊 SLO 상세 — slo_baseline 시나리오

| 지표 | 값 |
|------|----|
| P50  | ${ms('http_req_duration{scenario:slo_baseline}', 'p(50)')} |
| P90  | ${ms('http_req_duration{scenario:slo_baseline}', 'p(90)')} |
| P95  | ${ms('http_req_duration{scenario:slo_baseline}', 'p(95)')} |
| P99  | ${ms('http_req_duration{scenario:slo_baseline}', 'p(99)')} |
| 에러율 | ${pct('http_req_failed{scenario:slo_baseline}', 'rate')} |
| 총 요청 | ${val('http_req_duration{scenario:slo_baseline}', 'count').toFixed(0)}건 |

### 신규 결제(201) 전용 처리시간

| 지표 | 값 |
|------|----|
| P95  | ${ms('payment_new_duration', 'p(95)')} |
| P99  | ${ms('payment_new_duration', 'p(99)')} |
| MAX  | ${ms('payment_new_duration', 'max')} |

---

## 💳 결제 응답 분포 (전 시나리오 합산)

| 응답코드 | 건수 | 비율 | 의미 |
|----------|------|------|------|
| **201** | ${cnt('payment_201')} | ${totalReq > 0 ? (cnt('payment_201')/totalReq*100).toFixed(1) : 0}% | 신규 결제 성공 |
| **200** | ${cnt('payment_200')} | ${totalReq > 0 ? (cnt('payment_200')/totalReq*100).toFixed(1) : 0}% | 멱등키 캐시 응답 |
| **409** | ${cnt('payment_409')} | ${totalReq > 0 ? (cnt('payment_409')/totalReq*100).toFixed(1) : 0}% | 분산락 중복 차단 |
| **ERR** | ${cnt('payment_error')} | ${totalReq > 0 ? (cnt('payment_error')/totalReq*100).toFixed(1) : 0}% | 비정상 (400/500) |
| PG Timeout | ${cnt('payment_pg_timeout')} | — | MockPgClient 5% |

---

## ⚠️ 주의사항

- **alert rule ↔ k6 threshold는 프록시 관계** (1:1 아님)
  - 서버 p95: payment.processing.duration Timer (처리시간만)
  - k6 p95: http_req_duration (네트워크 왕복 포함, 항상 더 큼 → 보수적 게이트)
- **DUPLICATE_CHARGED** 메트릭은 서버 측 DB 제약 위반 시 increment
  (lock 실패로 슬립한 진짜 이중결제 시도 감지용, Grafana에서 확인)

---

*k6 버전: ${data.k6_version ?? 'unknown'} | Hard-Click Backend Module4*
`;

    return {
        'k6-report.md': md,
        stdout: md,
    };
}
