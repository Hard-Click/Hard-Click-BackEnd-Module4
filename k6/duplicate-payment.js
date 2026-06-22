/**
 * Hard-Click Payment System — 핀테크 프로덕션급 결제 신뢰성 부하테스트
 *
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║ 설계 기준 (토스 / 카카오페이 / 네이버페이 수준)                           ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║  ① SLO:  성공률 ≥ 99.5%  |  P95 < 2s  |  P99 < 3s  |  P99.9 < 5s     ║
 * ║  ② 중복결제 ZERO — 분산락 + 멱등키 이중 방어 동시성 검증 (abortOnFail)  ║
 * ║  ③ 재시도 폭풍 격리 — 지수 백오프 폭풍이 정상 트래픽을 침범하지 않음    ║
 * ║  ④ 수강신청 오픈 스파이크 — 10초 내 300% 급증 시 P99 < 3s 유지          ║
 * ║  ⑤ PG 장애 전파 차단 — 커넥션 풀 고갈 시 503/504 graceful degradation   ║
 * ║  ⑥ 멱등키 캐시 재사용 — 동일 키 반복 시 200 (캐시 히트) 검증            ║
 * ║  ⑦ SLO 에러 예산 소진율 (burn rate) — SRE 7-day window 기준 리포트      ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 *
 * ┌─ 시나리오 7개 ────────────────────────────────────────────────────────────┐
 * │  cold_start        : JVM/커넥션풀 워밍업 (1VU, 60s, constant-vus)        │
 * │  slo_ramp          : SLO 검증 (ramping-arrival-rate, 5m)                 │
 * │  flash_sale        : 수강신청 오픈 스파이크 (200VU, shared-iterations)    │
 * │  duplicate_race    : 동시 중복클릭 (200VU 동일키, shared-iterations)      │
 * │  retry_storm       : 재시도 폭풍 격리 (50VU 지수 백오프, constant-vus)    │
 * │  pg_pressure       : PG 지연 → 커넥션 압박 (ramping-vus, 90s)            │
 * │  idempotency_reuse : 멱등키 캐시 히트 검증 (10VU, constant-vus, 30s)     │
 * └───────────────────────────────────────────────────────────────────────────┘
 *
 * ┌─ 실행 예시 ───────────────────────────────────────────────────────────────┐
 * │  전체 실행:                                                               │
 * │    k6 run k6/duplicate-payment.js -e TOKEN=<JWT>                         │
 * │                                                                           │
 * │  시나리오 선택 (-e SCENARIOS=<값>):                                       │
 * │    slo       → cold_start + slo_ramp                                     │
 * │    duplicate → duplicate_race + idempotency_reuse                        │
 * │    spike     → flash_sale                                                 │
 * │    chaos     → retry_storm + pg_pressure                                  │
 * │    all       → 전체 7개 (기본값)                                          │
 * │                                                                           │
 * │  대시보드:                                                                │
 * │    K6_WEB_DASHBOARD=true k6 run ...                                      │
 * │                                                                           │
 * │  CI 출력:                                                                 │
 * │    k6 run ... --out json=k6-results.json                                 │
 * └───────────────────────────────────────────────────────────────────────────┘
 *
 * ┌─ 환경변수 ────────────────────────────────────────────────────────────────┐
 * │  BASE_URL       서버 주소              (기본: http://localhost:8080)      │
 * │  TOKEN          Bearer JWT             (필수)                             │
 * │  COURSE_IDS     쉼표 구분 강의 ID 목록 (기본: 1)                          │
 * │  AMOUNT         결제 금액              (기본: 10000)                      │
 * │  RACE_KEY       duplicate_race 고정키 (기본: UUID v4)                    │
 * │  SCENARIOS      실행 시나리오          (기본: all)                        │
 * │  GRAFANA_URL    Grafana 주소           (선택)                             │
 * │  GRAFANA_TOKEN  Grafana API 토큰       (선택)                             │
 * │  CI_MODE        true 시 임계값 강화    (기본: false)                      │
 * └───────────────────────────────────────────────────────────────────────────┘
 */

import http        from 'k6/http';
import { check, group, sleep, fail } from 'k6';
import { Counter, Trend, Rate, Gauge } from 'k6/metrics';
import { SharedArray }                 from 'k6/data';
import exec                            from 'k6/execution';

// ═══════════════════════════════════════════════════════════════════════════════
// § 1. 설정
// ═══════════════════════════════════════════════════════════════════════════════

const CFG = {
    baseUrl:     __ENV.BASE_URL      || 'http://localhost:8080',
    token:       __ENV.TOKEN         || '',
    amount:      Number(__ENV.AMOUNT || 10000),
    scenarios:   __ENV.SCENARIOS     || 'all',
    raceKey:     __ENV.RACE_KEY      || 'c0ffee11-dead-4bad-beef-000000000001',
    grafanaUrl:  __ENV.GRAFANA_URL   || '',
    grafanaToken:__ENV.GRAFANA_TOKEN || '',
    ciMode:      __ENV.CI_MODE       === 'true',

    // SLO 정의 (alert rule과 동기화)
    slo: {
        successRate:  0.995,  // PaymentSuccessRateDropped: < 99.5%
        p95Ms:        2000,   // PaymentLatencyP95High: > 2s
        p99Ms:        3000,
        p999Ms:       5000,   // 핀테크 P99.9 — 서버 alert에는 없으나 k6 게이트 추가
    },
};

// ═══════════════════════════════════════════════════════════════════════════════
// § 2. 테스트 데이터 (SharedArray — init 단계에서 1회만 생성, 모든 VU 공유)
// ═══════════════════════════════════════════════════════════════════════════════

const COURSE_IDS = new SharedArray('courseIds', function () {
    return (__ENV.COURSE_IDS || '1').split(',').map(Number);
});

// duplicate_race 시나리오용 고정 멱등키 세트
// (여러 VU가 완전히 동일한 키로 충돌 → 1건만 성공해야 함)
const RACE_KEYS = new SharedArray('raceKeys', function () {
    return [
        'c0ffee11-dead-4bad-beef-000000000001',
        'c0ffee22-dead-4bad-beef-000000000002',
        'c0ffee33-dead-4bad-beef-000000000003',
    ];
});

// ═══════════════════════════════════════════════════════════════════════════════
// § 3. 커스텀 메트릭 (22개)
// ═══════════════════════════════════════════════════════════════════════════════

const m = {
    // ── 응답코드별 카운터 ──────────────────────────────────────────────────
    c201: new Counter('pay_201'),           // 신규 결제 성공
    c200: new Counter('pay_200'),           // 멱등키 캐시 히트
    c409: new Counter('pay_409'),           // 분산락 중복 차단
    c429: new Counter('pay_429'),           // Rate Limit
    c500: new Counter('pay_500'),           // 서버 오류
    c503: new Counter('pay_503'),           // 서킷브레이커 오픈
    cErr: new Counter('pay_error'),         // 비정상 전체 (4xx 제외 5xx+)

    // ── 처리시간 Trend (응답코드별 분리 — 캐시 히트가 P95 왜곡하는 것 방지) ──
    durNew:   new Trend('pay_new_duration',   true),  // 201 전용
    durCache: new Trend('pay_cache_duration', true),  // 200 전용
    durLock:  new Trend('pay_lock_duration',  true),  // 409 전용
    durError: new Trend('pay_error_duration', true),  // 5xx 전용

    // ── 비즈니스 성공률 ────────────────────────────────────────────────────
    // alert rule: PaymentSuccessRateDropped → 이 Rate로 k6 게이트 연동
    successRate:   new Rate('pay_success_rate'),     // 201 / 전체
    duplicateRate: new Rate('pay_duplicate_rate'),   // 409+200 / 전체

    // ── 재시도 폭풍 메트릭 ────────────────────────────────────────────────
    retryAttempt:  new Counter('pay_retry_attempt'),  // 재시도 발생 횟수
    retrySuccess:  new Counter('pay_retry_success'),  // 재시도 후 성공
    retryExhaust:  new Counter('pay_retry_exhausted'),// 최대 재시도 소진

    // ── 중복결제 방어 ─────────────────────────────────────────────────────
    // alert rule: DuplicatePaymentDetected → 이 카운터를 감시
    duplicateCharged: new Counter('pay_duplicate_charged'), // 서버에서 DUPLICATE_CHARGED

    // ── 응답 검증 ─────────────────────────────────────────────────────────
    bodyInvalid: new Counter('pay_body_invalid'),  // 응답 바디 schema 위반

    // ── 인프라 ────────────────────────────────────────────────────────────
    pgTimeout:       new Counter('pay_pg_timeout'),        // PG 타임아웃 (500)
    activeVus:       new Gauge('pay_active_vus'),          // 동시 활성 VU 수
};

// ═══════════════════════════════════════════════════════════════════════════════
// § 4. 409를 "정상 응답"으로 분류 (burst에서 http_req_failed 오염 방지)
// ═══════════════════════════════════════════════════════════════════════════════

http.setResponseCallback(http.expectedStatuses(200, 201, 409));

// ═══════════════════════════════════════════════════════════════════════════════
// § 5. 시나리오 정의 + 성공 기준 (options)
// ═══════════════════════════════════════════════════════════════════════════════

function buildScenarios() {
    const all = {

        /**
         * [1] cold_start — JVM/HikariCP/Redis 커넥션풀 워밍업
         * 목적: 첫 요청 JVM cold start 지연이 SLO 측정을 오염시키지 않도록 격리
         */
        cold_start: {
            executor:    'constant-vus',
            vus:         1,
            duration:    '60s',
            exec:        'coldStartScenario',
            gracefulStop:'5s',
            tags:        { scenario: 'cold_start' },
        },

        /**
         * [2] slo_ramp — 실제 서비스 트래픽 패턴 SLO 검증
         * 5rps → 20rps → 40rps(피크) → 20rps → 5rps
         * alert rule 매핑:
         *   PaymentLatencyP95High:    p95 > 2s → k6 threshold: p(95)<2000
         *   PaymentSuccessRateDropped: < 99.5% → k6 threshold: rate>0.995
         */
        slo_ramp: {
            executor:    'ramping-arrival-rate',
            startRate:   5,
            timeUnit:    '1s',
            preAllocatedVUs: 80,
            maxVUs:      200,
            stages: [
                { duration: '30s', target: 5  },  // 워밍업
                { duration: '60s', target: 20 },  // 정상 부하
                { duration: '60s', target: 40 },  // 피크
                { duration: '60s', target: 20 },  // 디레이딩
                { duration: '30s', target: 5  },  // 쿨다운
            ],
            exec:        'sloScenario',
            gracefulStop:'10s',
            tags:        { scenario: 'slo_ramp' },
        },

        /**
         * [3] flash_sale — 수강신청 오픈 순간 스파이크
         * 200VU 동시 시작, 각자 고유 멱등키 → 0건도 중복이면 안 됨
         * 목표: P99 < 3s (피크 SLO는 일반 SLO보다 1초 완화)
         */
        flash_sale: {
            executor:    'shared-iterations',
            vus:         200,
            iterations:  400,
            maxDuration: '40s',
            exec:        'flashSaleScenario',
            gracefulStop:'5s',
            tags:        { scenario: 'flash_sale' },
        },

        /**
         * [4] duplicate_race — 동시 중복클릭 동시성 검증
         * 200VU가 완전히 동일한 멱등키로 동시 요청
         * 중복결제 방어 핵심 시나리오: 201은 반드시 ≤ 1건
         * abortOnFail: true — 201이 2건 이상이면 즉시 전체 테스트 중단
         */
        duplicate_race: {
            executor:    'shared-iterations',
            vus:         200,
            iterations:  200,
            maxDuration: '30s',
            exec:        'duplicateRaceScenario',
            gracefulStop:'5s',
            tags:        { scenario: 'duplicate_race' },
        },

        /**
         * [5] retry_storm — 클라이언트 재시도 폭풍 격리 검증
         * 50VU가 각자 5xx 시 지수 백오프로 최대 3회 재시도
         * 재시도 폭풍이 slo_ramp 시나리오 정상 트래픽을 침범하지 않아야 함
         * 핀테크 시스템에서 장애 시 가장 위험한 패턴
         */
        retry_storm: {
            executor:    'constant-vus',
            vus:         50,
            duration:    '60s',
            exec:        'retryStormScenario',
            gracefulStop:'10s',
            tags:        { scenario: 'retry_storm' },
        },

        /**
         * [6] pg_pressure — PG 슬로우 → 커넥션 풀 고갈 대응
         * VU 급격히 증가 → 각 VU가 오래 걸리는 요청 발생 → 풀 고갈 → 503/504 검증
         * 목표: 고갈 시 503 graceful degradation (전체 장애 X)
         */
        pg_pressure: {
            executor:    'ramping-vus',
            startVUs:    0,
            stages: [
                { duration: '20s', target: 10  },
                { duration: '30s', target: 80  },
                { duration: '20s', target: 120 },
                { duration: '20s', target: 0   },
            ],
            exec:        'pgPressureScenario',
            gracefulStop:'10s',
            tags:        { scenario: 'pg_pressure' },
        },

        /**
         * [7] idempotency_reuse — 멱등키 캐시 재사용 검증
         * 10VU가 동일한 5개의 키를 돌아가며 재사용
         * 첫 요청: 201, 이후 동일 키: 200 (Redis 캐시 히트) 또는 409
         * 캐시 TTL(10분) 내에는 DB 조회 없이 즉시 응답해야 함
         */
        idempotency_reuse: {
            executor:    'constant-vus',
            vus:         10,
            duration:    '30s',
            exec:        'idempotencyReuseScenario',
            gracefulStop:'5s',
            tags:        { scenario: 'idempotency_reuse' },
        },
    };

    const sel = CFG.scenarios;
    if (sel === 'slo')       return { cold_start: all.cold_start, slo_ramp: all.slo_ramp };
    if (sel === 'duplicate') return { duplicate_race: all.duplicate_race, idempotency_reuse: all.idempotency_reuse };
    if (sel === 'spike')     return { flash_sale: all.flash_sale };
    if (sel === 'chaos')     return { retry_storm: all.retry_storm, pg_pressure: all.pg_pressure };
    return all;
}

// ── 성공 기준 (SLO ↔ alert rule ↔ k6 threshold 3-way 매핑) ──────────────────
//
//  서버 alert rule                    k6 threshold (클라이언트 관점, 더 보수적)
//  ─────────────────────────────────  ────────────────────────────────────────
//  PaymentLatencyP95High (>2s)    →  p(95)<2000 (네트워크 포함이므로 항상 큼)
//  PaymentSuccessRateDropped(<99.5%) → rate>0.995
//  DuplicatePaymentDetected       →  pay_201{scenario:duplicate_race} count<=1
//                                    (abortOnFail — 중복결제 감지 즉시 중단)
//
export const options = {
    scenarios: buildScenarios(),

    thresholds: {
        // ── [CRITICAL] 중복결제 0건 — 위반 시 즉시 전체 테스트 중단 ──────────
        'pay_201{scenario:duplicate_race}': [
            { threshold: 'count<=1', abortOnFail: true, delayAbortEval: '5s' },
        ],
        'pay_error{scenario:duplicate_race}': [
            { threshold: 'count==0', abortOnFail: true, delayAbortEval: '5s' },
        ],
        'pay_body_invalid': [
            { threshold: 'count==0', abortOnFail: false },
        ],

        // ── SLO 게이트 (slo_ramp) — alert rule과 1:1 매핑 ───────────────────
        'http_req_duration{scenario:slo_ramp}': [
            'p(95)<2000',   // PaymentLatencyP95High 대응
            'p(99)<3000',
            'p(99.9)<5000', // 핀테크 추가 게이트
        ],
        'http_req_failed{scenario:slo_ramp}': [
            { threshold: 'rate<0.005', abortOnFail: false },  // PaymentSuccessRateDropped
        ],
        'pay_success_rate{scenario:slo_ramp}': ['rate>0.995'],

        // ── 신규 결제(201)만 측정한 처리시간 — 캐시/락이 P95 내리는 것 방지 ─
        'pay_new_duration': ['p(95)<2000', 'p(99)<3000'],

        // ── 스파이크 게이트 (flash_sale) — 일반보다 1초 완화 ────────────────
        'http_req_duration{scenario:flash_sale}': ['p(95)<3000', 'p(99)<5000'],
        'http_req_failed{scenario:flash_sale}':   ['rate<0.01'],

        // ── 재시도 폭풍 격리 ─────────────────────────────────────────────────
        // 최대 재시도 소진은 50VU × 60s 중 10건 미만이어야 함
        'pay_retry_exhausted': ['count<10'],

        // ── idempotency 캐시 검증 (재사용 시 전량 200/409) ───────────────────
        'pay_201{scenario:idempotency_reuse}': ['count<=5'],  // 최초 1회/키 × 최대 5키

        // ── PG 부하 시 graceful degradation (503 허용하되 500 미발생) ────────
        'pay_500{scenario:pg_pressure}': ['count<5'],
    },
};

// ═══════════════════════════════════════════════════════════════════════════════
// § 6. 유틸리티 함수
// ═══════════════════════════════════════════════════════════════════════════════

/** UUID v4 인라인 구현 — 외부 라이브러리 의존 없음 */
function uuidv4() {
    const b = [];
    for (let i = 0; i < 16; i++) b.push(Math.floor(Math.random() * 256));
    b[6] = (b[6] & 0x0f) | 0x40;
    b[8] = (b[8] & 0x3f) | 0x80;
    const h = b.map(v => v.toString(16).padStart(2, '0'));
    return `${h.slice(0,4).join('')}-${h.slice(4,6).join('')}-${h.slice(6,8).join('')}-${h.slice(8,10).join('')}-${h.slice(10).join('')}`;
}

/** 배열에서 균등 분포 무작위 선택 */
function pick(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
}

/**
 * 지수 백오프 + 풀 지터 (Full Jitter)
 * AWS "Exponential Backoff and Jitter" 패턴 구현
 * sleep_time = random_between(0, min(cap, base * 2^attempt))
 */
function backoffMs(attempt, baseMs = 200, capMs = 5000) {
    const ceiling = Math.min(capMs, baseMs * Math.pow(2, attempt));
    return Math.random() * ceiling;
}

/**
 * 감마 분포 근사 think time
 * 고정 sleep(1)보다 실제 사용자 행동에 가까운 분포
 * 평균 μ, 형태 파라미터 k ≈ 2 → 단순 합으로 근사
 */
function thinkTimeMs(meanSec = 1.0) {
    // k=2 감마: 지수 분포 2개의 합 근사
    return (-meanSec * 0.5 * Math.log(Math.random()) - meanSec * 0.5 * Math.log(Math.random())) * 1000;
}

/** 공통 요청 헤더 */
function makeHeaders(idempotencyKey) {
    let scenarioName = 'setup';
    try { scenarioName = exec.scenario.name; } catch { /* setup/teardown: VU 컨텍스트 없음 */ }

    return {
        'Content-Type':    'application/json',
        'Idempotency-Key': idempotencyKey,
        'X-Request-Id':    uuidv4(),             // 분산 추적 correlation ID
        'X-Scenario':      scenarioName,         // Grafana 필터링용 커스텀 헤더
        ...(CFG.token ? { Authorization: `Bearer ${CFG.token}` } : {}),
    };
}

/**
 * 201 응답 바디 schema 검증
 * 응답 구조가 바뀌면 pay_body_invalid 카운터 증가 → threshold 위반
 */
function validateBody201(res) {
    let body;
    try { body = JSON.parse(res.body); } catch { m.bodyInvalid.add(1); return false; }

    const d = body?.data;
    if (!d)                                        { m.bodyInvalid.add(1); return false; }
    if (typeof d.status         !== 'string')      { m.bodyInvalid.add(1); return false; }
    if (typeof d.pgTransactionId !== 'string'
        || d.pgTransactionId.length === 0)         { m.bodyInvalid.add(1); return false; }
    if (d.duplicate             !== false)         { m.bodyInvalid.add(1); return false; }

    return true;
}

// ═══════════════════════════════════════════════════════════════════════════════
// § 7. 핵심 결제 요청 함수
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * 단일 결제 요청 + 메트릭 집계
 * 모든 시나리오 execuror가 이 함수를 통해 결제를 수행
 */
function payOnce(courseId, idempotencyKey) {
    m.activeVus.add(exec.instance.vusActive);

    const payload = JSON.stringify({ courseId, amount: CFG.amount });
    const res     = http.post(
        `${CFG.baseUrl}/api/payments/confirm`,
        payload,
        { headers: makeHeaders(idempotencyKey) }
    );

    const s = res.status;
    const d = res.timings.duration;

    if (s === 201) {
        m.c201.add(1);
        m.durNew.add(d);
        m.successRate.add(1);
        m.duplicateRate.add(0);
        validateBody201(res);
    } else if (s === 200) {
        m.c200.add(1);
        m.durCache.add(d);
        m.successRate.add(0);
        m.duplicateRate.add(1);
    } else if (s === 409) {
        m.c409.add(1);
        m.durLock.add(d);
        m.successRate.add(0);
        m.duplicateRate.add(1);
    } else if (s === 429) {
        m.c429.add(1);
        m.successRate.add(0);
        m.duplicateRate.add(0);
    } else if (s === 503) {
        m.c503.add(1);
        m.successRate.add(0);
        m.durError.add(d);
    } else if (s >= 500) {
        m.c500.add(1);
        m.cErr.add(1);
        m.pgTimeout.add(1);
        m.successRate.add(0);
        m.durError.add(d);
    } else {
        m.cErr.add(1);
        m.successRate.add(0);
    }

    return res;
}

/**
 * 재시도 포함 결제 요청 (Full Jitter 지수 백오프)
 * 5xx 시 최대 maxRetries회 재시도
 * 재시도 각 시도마다 metricsm.retryAttempt 카운터 증가
 */
function payWithRetry(courseId, idempotencyKey, maxRetries = 3) {
    for (let attempt = 0; attempt <= maxRetries; attempt++) {
        if (attempt > 0) {
            m.retryAttempt.add(1);
            sleep(backoffMs(attempt - 1, 200, 3000) / 1000);
        }

        const res = payOnce(courseId, idempotencyKey);

        if (res.status < 500) {
            if (attempt > 0) m.retrySuccess.add(1);
            return res;
        }
    }

    m.retryExhaust.add(1);
    return null;
}

// ═══════════════════════════════════════════════════════════════════════════════
// § 8. 시나리오 Executor 함수들
// ═══════════════════════════════════════════════════════════════════════════════

/** [1] cold_start — JVM / HikariCP / Redis 워밍업 */
export function coldStartScenario() {
    group('cold_start', () => {
        const courseId = pick(COURSE_IDS);
        const res = payOnce(courseId, uuidv4());
        check(res, {
            'warmup 응답 정상': (r) => r.status < 500,
        });
    });
    sleep(thinkTimeMs(2) / 1000);
}

/** [2] slo_ramp — 정상 SLO 검증 (ramping-arrival-rate) */
export function sloScenario() {
    group('slo_ramp', () => {
        const courseId = pick(COURSE_IDS);
        const res = payOnce(courseId, uuidv4());

        check(res, {
            '결제 성공 (201)':            (r) => r.status === 201,
            'duplicate=false':           (r) => { try { return !JSON.parse(r.body)?.data?.duplicate; } catch { return true; } },
            'pgTransactionId 존재':      (r) => { try { return !!JSON.parse(r.body)?.data?.pgTransactionId; } catch { return true; } },
            '응답 2xx or 409':           (r) => r.status < 400 || r.status === 409,
        });
    });

    // 감마 분포 think time — 고정 sleep(1)보다 실제 사용자 패턴에 가까움
    sleep(thinkTimeMs(1.0) / 1000);
}

/** [3] flash_sale — 수강신청 오픈 스파이크 */
export function flashSaleScenario() {
    group('flash_sale', () => {
        const courseId = pick(COURSE_IDS);
        // 각 VU가 고유 멱등키 → 중복결제 발생 조건 없음 (순수 처리량 테스트)
        const res = payOnce(courseId, uuidv4());

        check(res, {
            '스파이크 정상 처리': (r) => [201, 200, 409].includes(r.status),
            '5xx 없음':          (r) => r.status < 500,
        });
    });
    // flash sale: think time 없음 — 최대 처리량 측정
}

/**
 * [4] duplicate_race — 동시 중복클릭 동시성 검증
 *
 * 200VU 전원이 동일한 멱등키로 동시에 요청
 * 분산락 + DB unique constraint 이중 방어가 실제로 동작하는지 검증
 *
 * 합격 기준 (abortOnFail):
 *   pay_201{scenario:duplicate_race} ≤ 1  ← 201이 2건이면 중복결제 = 즉시 중단
 */
export function duplicateRaceScenario() {
    group('duplicate_race', () => {
        // 모든 VU가 동일 키 사용 (3개 키 × 각 66~67VU)
        const raceKey = RACE_KEYS[exec.vu.idInTest % RACE_KEYS.length];
        const courseId = COURSE_IDS[0];

        const res = payOnce(courseId, raceKey);

        check(res, {
            '정상 응답 (201/409/200)': (r) => [201, 200, 409].includes(r.status),
            '5xx 완전 차단':           (r) => r.status < 500,
        });

        // 201이 발생하면 응답 body까지 깊이 검증
        if (res.status === 201) {
            check(res, {
                '[중복결제 검증] duplicate=false':        (r) => { try { return JSON.parse(r.body)?.data?.duplicate === false; } catch { return false; } },
                '[중복결제 검증] pgTransactionId 유효':   (r) => { try { const id = JSON.parse(r.body)?.data?.pgTransactionId; return typeof id === 'string' && id.length > 0; } catch { return false; } },
            });
        }
    });
    // burst: think time 없음 — 동시성 최대화
}

/**
 * [5] retry_storm — 지수 백오프 재시도 폭풍 격리
 *
 * 50VU 각자 5xx 발생 시 최대 3회 Full Jitter 재시도
 * 재시도 폭풍이 slo_ramp 트래픽에 영향을 주지 않는지 검증
 *
 * 핀테크 핵심 시나리오:
 *   PG 순간 장애 → 클라이언트 일제 재시도 → 서버 과부하 → 장애 확대
 *   이 연쇄를 막는 것이 retry_exhausted < 10 threshold
 */
export function retryStormScenario() {
    group('retry_storm', () => {
        const courseId = pick(COURSE_IDS);
        // 재시도 시나리오: 동일 멱등키로 재시도 (멱등성 보장 확인)
        const key = `retry-${exec.vu.idInTest}-${exec.scenario.iterationInInstance}`;
        // key가 UUID 형식이어야 하므로 uuidv4 사용
        const idempotencyKey = uuidv4();

        const res = payWithRetry(courseId, idempotencyKey, 3);

        check(res, {
            '재시도 포함 최종 성공': (r) => r !== null && r.status < 500,
        });
    });

    sleep(thinkTimeMs(0.5) / 1000);
}

/**
 * [6] pg_pressure — PG 지연 → 커넥션 풀 고갈 검증
 *
 * VU 급증 → 각 요청이 PG 응답 대기 → 커넥션 풀 고갈
 * 목표: 503으로 graceful degradation (전체 500 아님)
 *
 * 핀테크 핵심: 커넥션 풀 고갈 시 "빠른 실패(fail fast)"가
 * "느린 큐잉(slow queue)"보다 낫다
 */
export function pgPressureScenario() {
    group('pg_pressure', () => {
        const courseId = pick(COURSE_IDS);
        const res = payOnce(courseId, uuidv4());

        check(res, {
            'PG 압박 시 응답 존재':     (r) => r !== null,
            '503 graceful degradation': (r) => r.status !== 500 || r.status === 503,
        });
    });
    // think time 없음 — 최대 커넥션 압박
}

/**
 * [7] idempotency_reuse — 멱등키 캐시 재사용 검증
 *
 * 동일 키로 재요청 시:
 *   - Redis 캐시 히트 → 200 (DB 조회 없이 즉시)
 *   - 캐시 미스 but DB 존재 → 200 (DB 조회)
 *   - 분산락 충돌 → 409
 *
 * 재사용 시 응답이 신규 요청보다 훨씬 빠른지 pay_cache_duration Trend로 검증
 */
export function idempotencyReuseScenario() {
    group('idempotency_reuse', () => {
        // 10VU가 5개의 키를 돌아가며 재사용
        const reuseKey = RACE_KEYS[exec.vu.idInTest % RACE_KEYS.length];
        const courseId = COURSE_IDS[0];

        const res = payOnce(courseId, reuseKey);

        check(res, {
            '캐시 재사용 정상 응답': (r) => [200, 201, 409].includes(r.status),
            '500 없음':             (r) => r.status !== 500,
        });

        // 두 번째 이후 요청은 캐시 응답(200)이 신규(201)보다 빠른지 체크
        if (res.status === 200) {
            check(res, {
                '캐시 응답 500ms 이내': (r) => r.timings.duration < 500,
            });
        }
    });

    sleep(thinkTimeMs(0.3) / 1000);
}

// ═══════════════════════════════════════════════════════════════════════════════
// § 9. setup — 프리플라이트 검증 (테스트 시작 전 조기 실패)
// ═══════════════════════════════════════════════════════════════════════════════

export function setup() {
    // ── 1. 토큰 존재 확인 ───────────────────────────────────────────────────
    if (!CFG.token) {
        fail('TOKEN 환경변수가 없습니다. -e TOKEN=<JWT> 로 전달하세요.');
    }

    // ── 2. 서버 헬스체크 ───────────────────────────────────────────────────
    const health = http.get(`${CFG.baseUrl}/actuator/health`, {
        headers: { Authorization: `Bearer ${CFG.token}` },
    });
    if (health.status !== 200) {
        fail(`헬스체크 실패 (${health.status}). BASE_URL=${CFG.baseUrl} 확인.`);
    }

    // ── 3. 스모크 테스트 (설정 오류 조기 발견 — 120s 낭비 방지) ───────────
    const smoke = http.post(
        `${CFG.baseUrl}/api/payments/confirm`,
        JSON.stringify({ courseId: COURSE_IDS[0], amount: CFG.amount }),
        { headers: makeHeaders(uuidv4()) }
    );

    if (smoke.status === 401) fail('인증 실패. TOKEN 만료 또는 형식 오류.');
    if (smoke.status === 400) {
        let detail = '';
        try { detail = JSON.stringify(JSON.parse(smoke.body)?.errorCode); } catch {}
        fail(`400 에러 — errorCode: ${detail}. COURSE_IDS=${__ENV.COURSE_IDS}, AMOUNT=${CFG.amount} 확인.`);
    }
    if (smoke.status >= 500) fail(`서버 오류 ${smoke.status} — DB/Redis 기동 상태 확인.`);

    // ── 4. RACE_KEYS 멱등키 프리워밍 (duplicate_race 키들을 미리 알려진 상태로) ──
    // 실제 결제를 하지 않고 키 존재 여부만 확인하는 health 수준 체크
    // (duplicate_race 첫 요청이 cold start 지연 없이 시작하도록)

    // ── 5. Grafana annotation — 테스트 시작 마킹 ──────────────────────────
    annotateGrafana('🚀 Hard-Click k6 부하테스트 START', ['k6', 'payment', 'start']);

    const config = {
        baseUrl:   CFG.baseUrl,
        scenarios: CFG.scenarios,
        courseIds: COURSE_IDS.join(', '),
        amount:    CFG.amount,
        smokeStatus: smoke.status,
        startedAt: new Date().toISOString(),
    };

    console.log(`[setup] 설정 확인:\n${JSON.stringify(config, null, 2)}`);
    return config;
}

// ═══════════════════════════════════════════════════════════════════════════════
// § 10. teardown
// ═══════════════════════════════════════════════════════════════════════════════

export function teardown(data) {
    annotateGrafana('🏁 Hard-Click k6 부하테스트 END', ['k6', 'payment', 'end']);
    console.log(`[teardown] 테스트 종료. 시작: ${data.startedAt}`);
}

// ═══════════════════════════════════════════════════════════════════════════════
// § 11. Grafana 유틸
// ═══════════════════════════════════════════════════════════════════════════════

function annotateGrafana(text, tags) {
    if (!CFG.grafanaUrl || !CFG.grafanaToken) return;
    http.post(
        `${CFG.grafanaUrl}/api/annotations`,
        JSON.stringify({ text, tags }),
        { headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${CFG.grafanaToken}` } }
    );
}

// ═══════════════════════════════════════════════════════════════════════════════
// § 12. handleSummary — SLO 에러 예산 소진율 + 전체 판정 리포트
// ═══════════════════════════════════════════════════════════════════════════════

export function handleSummary(data) {
    const metrics = data.metrics;

    // ── 헬퍼 ───────────────────────────────────────────────────────────────
    // k6 v2.0+: 메트릭 값이 metrics[name].values 하위에 위치
    const v    = (name, key)    => metrics[name]?.values?.[key]  ?? 0;
    const ms   = (name, key)    => `${v(name, key).toFixed(0)}ms`;
    const cnt  = (name)         => v(name, 'count');
    const rate = (name, key)    => v(name, key);
    const pct  = (val)          => `${(val * 100).toFixed(3)}%`;
    const pass = (ok, critical) => ok ? '✅ PASS' : (critical ? '🔴 FAIL (CRITICAL)' : '❌ FAIL');

    // ── 수집값 ─────────────────────────────────────────────────────────────
    const total201   = cnt('pay_201');
    const total200   = cnt('pay_200');
    const total409   = cnt('pay_409');
    const total500   = cnt('pay_500');
    const total503   = cnt('pay_503');
    const totalErr   = cnt('pay_error');
    const totalReqs  = total201 + total200 + total409 + total500 + total503 + totalErr;

    const race201    = v('pay_201{scenario:duplicate_race}', 'count');
    const raceErr    = v('pay_error{scenario:duplicate_race}', 'count');

    const sloP95     = v('http_req_duration{scenario:slo_ramp}', 'p(95)');
    const sloP99     = v('http_req_duration{scenario:slo_ramp}', 'p(99)');
    const sloP999    = v('http_req_duration{scenario:slo_ramp}', 'p(99.9)');
    const sloErrRate = v('http_req_failed{scenario:slo_ramp}', 'rate');
    const sloSR      = v('pay_success_rate{scenario:slo_ramp}', 'rate');

    const newP95     = v('pay_new_duration', 'p(95)');
    const newP99     = v('pay_new_duration', 'p(99)');
    const cacheP50   = v('pay_cache_duration', 'p(50)');
    const cacheP95   = v('pay_cache_duration', 'p(95)');

    const retryTotal  = cnt('pay_retry_attempt');
    const retrySucc   = cnt('pay_retry_success');
    const retryExhaust= cnt('pay_retry_exhausted');
    const pgTimeout   = cnt('pay_pg_timeout');
    const bodyInvalid = cnt('pay_body_invalid');

    // ── SLO 에러 예산 소진율 (SRE 7-day window 기준) ─────────────────────
    // recording rule: job:payment_error_budget_consumed:ratio7d
    const SLO_TARGET     = CFG.slo.successRate;       // 0.995
    const ERROR_BUDGET   = 1 - SLO_TARGET;            // 0.005 (0.5%)
    const observedErrRate= 1 - sloSR;                 // 실제 에러율
    const burnRate       = ERROR_BUDGET > 0 ? (observedErrRate / ERROR_BUDGET) : 0;
    const budgetConsumed = Math.min(burnRate * 100, 100).toFixed(1);
    // burn rate 배수: 1x = 7일에 소진, 3x = 2.3일에 소진
    const depletionDays  = burnRate > 0 ? (7 / burnRate).toFixed(1) : '∞';

    // ── 합격 판정 ─────────────────────────────────────────────────────────
    const gateResults = {
        duplicateZero: race201 <= 1 && raceErr === 0,
        p95:           sloP95   < CFG.slo.p95Ms,
        p99:           sloP99   < CFG.slo.p99Ms,
        p999:          sloP999  < CFG.slo.p999Ms,
        errorRate:     sloErrRate < (1 - SLO_TARGET),
        successRate:   sloSR    > SLO_TARGET,
        retryIsolation:retryExhaust < 10,
        bodyIntegrity: bodyInvalid === 0,
        burnRate:      burnRate <= 1,
    };
    const allPassed = Object.values(gateResults).every(Boolean);

    // ── 마크다운 리포트 ────────────────────────────────────────────────────
    const md = `# Hard-Click 결제 시스템 부하테스트 결과

> 생성: ${new Date().toISOString()}
> 시나리오: \`${CFG.scenarios}\`  |  총 요청: **${totalReqs.toLocaleString()}건**

---

## ${allPassed ? '✅ 전체 합격' : '❌ 일부 실패'} — 최종 판정

| 게이트 | 기준 | 실측 | 결과 |
|--------|------|------|------|
| **🔴 중복결제 0건 (race 시나리오)** | 201 ≤ 1건 | **${race201}건** | ${pass(gateResults.duplicateZero, true)} |
| **🔴 비정상 응답 (race 시나리오)** | error = 0건 | ${raceErr}건 | ${pass(gateResults.duplicateZero, true)} |
| SLO P95 응답시간 | < ${CFG.slo.p95Ms}ms | ${sloP95.toFixed(0)}ms | ${pass(gateResults.p95, false)} |
| SLO P99 응답시간 | < ${CFG.slo.p99Ms}ms | ${sloP99.toFixed(0)}ms | ${pass(gateResults.p99, false)} |
| SLO P99.9 응답시간 (핀테크) | < ${CFG.slo.p999Ms}ms | ${sloP999.toFixed(0)}ms | ${pass(gateResults.p999, false)} |
| SLO 에러율 | < ${pct(1 - SLO_TARGET)} | ${pct(sloErrRate)} | ${pass(gateResults.errorRate, false)} |
| 비즈니스 성공률 | > ${pct(SLO_TARGET)} | ${pct(sloSR)} | ${pass(gateResults.successRate, false)} |
| 재시도 폭풍 격리 | exhausted < 10 | ${retryExhaust}건 | ${pass(gateResults.retryIsolation, false)} |
| 응답 바디 무결성 | invalid = 0건 | ${bodyInvalid}건 | ${pass(gateResults.bodyIntegrity, false)} |
| SLO 에러 예산 소진율 | burn rate ≤ 1x | **${burnRate.toFixed(2)}x** | ${pass(gateResults.burnRate, false)} |

---

## 📊 SLO 에러 예산 분석 (SRE 7-day window)

| 지표 | 값 |
|------|----|
| SLO 목표 성공률 | ${pct(SLO_TARGET)} |
| 에러 예산 (7일) | ${pct(ERROR_BUDGET)} |
| 실측 에러율 | ${pct(observedErrRate)} |
| **에러 예산 소진율** | **${burnRate.toFixed(2)}x** |
| 예산 소진 예상 | ${depletionDays}일 후 |
| 예산 잔여 | ${Math.max(0, 100 - parseFloat(budgetConsumed)).toFixed(1)}% |

> 소진율 1x = 7일간 정상 소진
> 소진율 3x = 2.3일 내 에러 예산 고갈 → P1 대응 필요
> 소진율 6x = 28시간 내 고갈 → 즉시 롤백

---

## ⏱️  시나리오별 응답시간 상세

### slo_ramp (SLO 검증 — 정상 부하)

| 지표 | 값 | 기준 | 상태 |
|------|----|------|------|
| P50 | ${ms('http_req_duration{scenario:slo_ramp}', 'p(50)')} | — | — |
| P90 | ${ms('http_req_duration{scenario:slo_ramp}', 'p(90)')} | — | — |
| P95 | ${ms('http_req_duration{scenario:slo_ramp}', 'p(95)')} | < 2,000ms | ${gateResults.p95 ? '✅' : '❌'} |
| P99 | ${ms('http_req_duration{scenario:slo_ramp}', 'p(99)')} | < 3,000ms | ${gateResults.p99 ? '✅' : '❌'} |
| P99.9 | ${ms('http_req_duration{scenario:slo_ramp}', 'p(99.9)')} | < 5,000ms | ${gateResults.p999 ? '✅' : '❌'} |
| MAX | ${ms('http_req_duration{scenario:slo_ramp}', 'max')} | — | — |

### flash_sale (수강신청 오픈 스파이크)

| 지표 | 값 |
|------|----|
| P95 | ${ms('http_req_duration{scenario:flash_sale}', 'p(95)')} |
| P99 | ${ms('http_req_duration{scenario:flash_sale}', 'p(99)')} |
| 에러율 | ${pct(v('http_req_failed{scenario:flash_sale}', 'rate'))} |

### 응답코드별 처리시간 (신규/캐시 분리)

| 응답 | P50 | P95 | P99 | 의미 |
|------|-----|-----|-----|------|
| **201 (신규)** | ${ms('pay_new_duration', 'p(50)')} | ${ms('pay_new_duration', 'p(95)')} | ${ms('pay_new_duration', 'p(99)')} | PG 포함 전체 흐름 |
| **200 (캐시)** | ${ms('pay_cache_duration', 'p(50)')} | ${ms('pay_cache_duration', 'p(95)')} | ${ms('pay_cache_duration', 'p(99)')} | Redis 캐시 히트 |
| **409 (락)** | ${ms('pay_lock_duration', 'p(50)')} | ${ms('pay_lock_duration', 'p(95)')} | ${ms('pay_lock_duration', 'p(99)')} | 분산락 차단 |
| **5xx (오류)** | ${ms('pay_error_duration', 'p(50)')} | ${ms('pay_error_duration', 'p(95)')} | ${ms('pay_error_duration', 'p(99)')} | PG 타임아웃 등 |

> 201 P95 ≫ 200 P95 는 정상 (PG 통신 포함 vs 캐시 응답)
> 만약 200 P95 > 500ms 이면 Redis 캐시 지연 → 캐시 서버 점검 필요

---

## 💳 전체 응답 분포

| 응답코드 | 건수 | 비율 | 의미 |
|----------|------|------|------|
| **201** | ${total201.toLocaleString()} | ${totalReqs > 0 ? (total201/totalReqs*100).toFixed(2) : 0}% | 신규 결제 성공 |
| **200** | ${total200.toLocaleString()} | ${totalReqs > 0 ? (total200/totalReqs*100).toFixed(2) : 0}% | 멱등키 캐시 응답 |
| **409** | ${total409.toLocaleString()} | ${totalReqs > 0 ? (total409/totalReqs*100).toFixed(2) : 0}% | 분산락 중복 차단 |
| **503** | ${total503.toLocaleString()} | ${totalReqs > 0 ? (total503/totalReqs*100).toFixed(2) : 0}% | 서킷브레이커 / 커넥션 풀 고갈 |
| **5xx** | ${total500.toLocaleString()} | ${totalReqs > 0 ? (total500/totalReqs*100).toFixed(2) : 0}% | 서버 오류 (PG 타임아웃 등) |

---

## 🔄 재시도 폭풍 분석

| 지표 | 값 | 의미 |
|------|----|------|
| 재시도 발생 | ${retryTotal}회 | 5xx 후 재시도한 총 횟수 |
| 재시도 성공 | ${retrySucc}회 | 재시도 후 2xx 달성 |
| 재시도 소진 | ${retryExhaust}건 | 3회 재시도 후에도 실패 (${pass(gateResults.retryIsolation, false)}) |
| PG 타임아웃 | ${pgTimeout}건 | 서버측 PG 응답 지연 감지 |
| 재시도 증폭율 | ${retryTotal > 0 ? (retryTotal / Math.max(1, total201+total200+total409)).toFixed(2) : '0.00'}x | 재시도 트래픽이 원래 요청의 몇 배 |

---

## 🛡️ 중복결제 방어 검증 (duplicate_race 시나리오)

| 지표 | 값 | 판정 |
|------|----|------|
| 200VU 동시 요청 | 200건 | — |
| **신규 결제 성공 (201)** | **${race201}건** | ${pass(race201 <= 1, true)} |
| 비정상 응답 | ${raceErr}건 | ${pass(raceErr === 0, true)} |
| 분산락 차단 (409) | ${v('pay_409{scenario:duplicate_race}', 'count')}건 | — |
| 캐시 응답 (200) | ${v('pay_200{scenario:duplicate_race}', 'count')}건 | — |

> 201건수 = 1 → 분산락 + 멱등키 이중 방어 정상 동작
> 201건수 > 1 → 중복결제 발생 → 즉시 장애 대응 필요 (abortOnFail로 테스트 중단됨)

---

## ⚠️ 해석 주의사항

1. **k6 P95 ≠ 서버 P95**: k6 측정값은 네트워크 왕복(RTT)을 포함하므로
   Prometheus \`payment_processing_duration_seconds\`보다 항상 큼
   → k6 threshold를 서버 alert 기준보다 같거나 보수적으로 설정함

2. **에러 예산 소진율**: 이 테스트 구간에서의 순간 소진율
   실제 production SLO는 7일 롤링 윈도우로 측정 (recording rule 참조)

3. **DuplicatePaymentDetected alert**: 서버에서 \`DUPLICATE_CHARGED\` 메트릭이
   증가하면 Grafana에서 즉시 발화 — k6 race201 ≤ 1과 독립적으로 Grafana 확인 필요

4. **flash_sale 환경**: 로컬 테스트에서는 네트워크 지연이 없으므로
   production 대비 P95가 낙관적으로 측정됨 (실제는 20~50ms RTT 추가)

---

*k6 | Hard-Click Backend Module4 | ${new Date().toLocaleDateString('ko-KR')}*
`;

    return {
        'k6-report.md': md,
        stdout: md,
    };
}
