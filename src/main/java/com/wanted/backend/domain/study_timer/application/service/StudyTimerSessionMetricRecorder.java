package com.wanted.backend.domain.study_timer.application.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudyTimerSessionMetricRecorder {

    private static final String SUCCESS_ERROR_CODE = "NONE";

    private final MeterRegistry meterRegistry;

    public void recordSuccess(String action) {
        record(action, SUCCESS_ERROR_CODE);
    }

    public void recordFailure(String action, String errorCode) {
        record(action, errorCode);
    }

    // metric 기록 실패가 핵심 트랜잭션(세션 저장)에 영향을 주면 안 되므로 예외를 여기서 흡수한다.
    private void record(String action, String errorCode) {
        try {
            Counter.builder("study_timer.session.result")
                    .tag("action", action)
                    .tag("status", errorCode.equals(SUCCESS_ERROR_CODE) ? "SUCCESS" : "FAILED")
                    .tag("errorCode", errorCode)
                    .register(meterRegistry)
                    .increment();
        } catch (Exception exception) {
            log.warn("[StudyTimerMetric] 기록 실패. action={}, errorCode={}", action, errorCode, exception);
        }
    }
}
