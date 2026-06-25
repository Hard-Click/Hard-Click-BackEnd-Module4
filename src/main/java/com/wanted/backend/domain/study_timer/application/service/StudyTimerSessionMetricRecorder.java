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

    public void recordSuccess(StudyTimerAction action) {
        record(action, SUCCESS_ERROR_CODE);
    }

    public void recordFailure(StudyTimerAction action, String errorCode) {
        record(action, errorCode);
    }

    // 호출부에서 "errorCode == null ? recordSuccess : recordFailure" 분기를 매번 복제하지 않도록
    // 그 판단 자체를 레코더가 떠맡는다. errorCode가 null이면 성공으로 기록한다.
    public void recordResult(StudyTimerAction action, String errorCode) {
        if (errorCode == null) {
            recordSuccess(action);
        } else {
            recordFailure(action, errorCode);
        }
    }

    // metric 기록 실패가 핵심 트랜잭션(세션 저장)에 영향을 주면 안 되므로 예외를 여기서 흡수한다.
    private void record(StudyTimerAction action, String errorCode) {
        try {
            Counter.builder("study_timer.session.result")
                    .tag("action", action.value())
                    .tag("status", SUCCESS_ERROR_CODE.equals(errorCode) ? "SUCCESS" : "FAILED")
                    .tag("errorCode", errorCode)
                    .register(meterRegistry)
                    .increment();
        } catch (Exception exception) {
            log.warn("[StudyTimerMetric] 기록 실패. action={}, errorCode={}", action, errorCode, exception);
        }
    }
}
