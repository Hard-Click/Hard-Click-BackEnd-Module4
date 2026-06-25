package com.wanted.backend.domain.learning_activity.application.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LearningActivityMetricRecorder {

    private static final String SUCCESS_ERROR_CODE = "NONE";

    private final MeterRegistry meterRegistry;

    public void recordSuccess(LearningActivityAction action) {
        record(action, SUCCESS_ERROR_CODE);
    }

    public void recordFailure(LearningActivityAction action, String errorCode) {
        record(action, errorCode);
    }

    // metric 기록 실패가 핵심 트랜잭션(진도 저장 등)에 영향을 주면 안 되므로 예외를 여기서 흡수한다.
    private void record(LearningActivityAction action, String errorCode) {
        try {
            Counter.builder("learning_activity.access.result")
                    .tag("action", action.value())
                    .tag("status", SUCCESS_ERROR_CODE.equals(errorCode) ? "SUCCESS" : "FAILED")
                    .tag("errorCode", errorCode)
                    .register(meterRegistry)
                    .increment();
        } catch (Exception exception) {
            log.warn("[LearningActivityMetric] 기록 실패. action={}, errorCode={}", action, errorCode, exception);
        }
    }
}
