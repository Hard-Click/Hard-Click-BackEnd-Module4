package com.wanted.backend.domain.learning_activity.application.policy;

import com.wanted.backend.domain.learning_activity.domain.model.VideoAccessInfo;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VideoAccessPolicyTest {

    private final VideoAccessPolicy policy = new VideoAccessPolicy();

    @Test
    void canPlayWhenVideoIsPreview() {
        VideoAccessInfo accessInfo = accessInfo("PUBLISHED", 10000, true);

        boolean result = policy.canPlay(accessInfo, false, false);

        assertThat(result).isTrue();
    }

    @Test
    void canPlayWhenCourseIsFree() {
        VideoAccessInfo accessInfo = accessInfo("PUBLISHED", 0, false);

        boolean result = policy.canPlay(accessInfo, false, false);

        assertThat(result).isTrue();
    }

    @Test
    void canPlayWhenMemberIsEnrolled() {
        VideoAccessInfo accessInfo = accessInfo("PUBLISHED", 10000, false);

        boolean result = policy.canPlay(accessInfo, true, false);

        assertThat(result).isTrue();
    }

    @Test
    void cannotPlayWhenCourseIsNotPublished() {
        VideoAccessInfo accessInfo = accessInfo("DRAFT", 0, true);

        boolean result = policy.canPlay(accessInfo, true, true);

        assertThat(result).isFalse();
    }

    @Test
    void cannotPlayWhenNoAccessConditionMatches() {
        VideoAccessInfo accessInfo = accessInfo("PUBLISHED", 10000, false);

        boolean result = policy.canPlay(accessInfo, false, false);

        assertThat(result).isFalse();
    }

    private VideoAccessInfo accessInfo(String courseStatus, Integer coursePrice, Boolean preview) {
        return new VideoAccessInfo(
                10L,
                20L,
                courseStatus,
                coursePrice,
                preview,
                "https://stream.example.com/video.m3u8",
                300
        );
    }
}
