package com.wanted.backend.domain.learning_activity.application.policy;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VideoCompletionPolicyTest {

    private final VideoCompletionPolicy policy = new VideoCompletionPolicy();

    @Test
    void 시청_시간이_90퍼센트_이상이면_완료할_수_있다() {
        assertThat(policy.canComplete(270, 300)).isTrue();
        assertThat(policy.canComplete(271, 300)).isTrue();
    }

    @Test
    void 시청_시간이_90퍼센트_미만이면_완료할_수_없다() {
        assertThat(policy.canComplete(269, 300)).isFalse();
    }

    @Test
    void 영상_길이가_유효하지_않으면_완료할_수_없다() {
        assertThat(policy.canComplete(10, 0)).isFalse();
        assertThat(policy.canComplete(10, null)).isFalse();
        assertThat(policy.canComplete(null, 300)).isFalse();
    }
}
