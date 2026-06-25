package com.wanted.backend.domain.study_timer.application.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

class StudyTimerSessionMetricRecorderTest {

    @Test
    void recordsSuccessCounterWithSuccessStatus() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        StudyTimerSessionMetricRecorder recorder = new StudyTimerSessionMetricRecorder(meterRegistry);

        recorder.recordSuccess(StudyTimerAction.START);

        Counter counter = meterRegistry.find("study_timer.session.result")
                .tag("action", "start")
                .tag("status", "SUCCESS")
                .tag("errorCode", "NONE")
                .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void recordsFailureCounterTaggedWithErrorCode() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        StudyTimerSessionMetricRecorder recorder = new StudyTimerSessionMetricRecorder(meterRegistry);

        recorder.recordFailure(StudyTimerAction.PAUSE, "STUDY_TIMER_SESSION_NOT_RUNNING");

        Counter counter = meterRegistry.find("study_timer.session.result")
                .tag("action", "pause")
                .tag("status", "FAILED")
                .tag("errorCode", "STUDY_TIMER_SESSION_NOT_RUNNING")
                .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void swallowsExceptionWhenMeterRegistryFailsSoCallerNeverSeesIt() {
        // record()의 핵심 책임: metric 기록 실패가 세션 저장 트랜잭션에 전파되면 안 된다.
        MeterRegistry failingRegistry = mock(MeterRegistry.class, invocation -> {
            throw new RuntimeException("meter registry boom");
        });
        StudyTimerSessionMetricRecorder recorder = new StudyTimerSessionMetricRecorder(failingRegistry);

        assertThatCode(() -> recorder.recordSuccess(StudyTimerAction.START)).doesNotThrowAnyException();
        assertThatCode(() -> recorder.recordFailure(StudyTimerAction.PAUSE, "STUDY_TIMER_SESSION_NOT_RUNNING"))
                .doesNotThrowAnyException();
    }
}
