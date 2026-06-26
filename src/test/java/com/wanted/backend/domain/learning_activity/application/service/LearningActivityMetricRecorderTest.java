package com.wanted.backend.domain.learning_activity.application.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

class LearningActivityMetricRecorderTest {

    @Test
    void recordsSuccessCounterWithSuccessStatus() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        LearningActivityMetricRecorder recorder = new LearningActivityMetricRecorder(meterRegistry);

        recorder.recordSuccess(LearningActivityAction.COMPLETE_VIDEO);

        Counter counter = meterRegistry.find("learning_activity.access.result")
                .tag("action", "completeVideo")
                .tag("status", "SUCCESS")
                .tag("errorCode", "NONE")
                .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void recordsFailureCounterTaggedWithErrorCode() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        LearningActivityMetricRecorder recorder = new LearningActivityMetricRecorder(meterRegistry);

        recorder.recordFailure(LearningActivityAction.VIDEO_ACCESS, "ENROLLMENT_REQUIRED");

        Counter counter = meterRegistry.find("learning_activity.access.result")
                .tag("action", "videoAccess")
                .tag("status", "FAILED")
                .tag("errorCode", "ENROLLMENT_REQUIRED")
                .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void recordResultRecordsSuccessWhenErrorCodeIsNull() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        LearningActivityMetricRecorder recorder = new LearningActivityMetricRecorder(meterRegistry);

        recorder.recordResult(LearningActivityAction.COURSE_PROGRESS, null);

        Counter counter = meterRegistry.find("learning_activity.access.result")
                .tag("action", "courseProgress")
                .tag("status", "SUCCESS")
                .tag("errorCode", "NONE")
                .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void recordResultRecordsFailureWhenErrorCodeIsPresent() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        LearningActivityMetricRecorder recorder = new LearningActivityMetricRecorder(meterRegistry);

        recorder.recordResult(LearningActivityAction.COURSE_PROGRESS, "ENROLLMENT_REQUIRED");

        Counter counter = meterRegistry.find("learning_activity.access.result")
                .tag("action", "courseProgress")
                .tag("status", "FAILED")
                .tag("errorCode", "ENROLLMENT_REQUIRED")
                .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void swallowsExceptionWhenMeterRegistryFailsSoCallerNeverSeesIt() {
        MeterRegistry failingRegistry = mock(MeterRegistry.class, invocation -> {
            throw new RuntimeException("meter registry boom");
        });
        LearningActivityMetricRecorder recorder = new LearningActivityMetricRecorder(failingRegistry);

        assertThatCode(() -> recorder.recordSuccess(LearningActivityAction.COMPLETE_VIDEO)).doesNotThrowAnyException();
        assertThatCode(() -> recorder.recordFailure(LearningActivityAction.VIDEO_ACCESS, "ENROLLMENT_REQUIRED"))
                .doesNotThrowAnyException();
    }
}
