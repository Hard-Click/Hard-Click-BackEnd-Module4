package com.wanted.backend.domain.learning_activity.infrastructure.playback;

import com.wanted.backend.domain.learning_activity.domain.model.VideoAccessInfo;
import com.wanted.backend.global.config.S3UrlPresigner;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class S3VideoPlaybackUrlAdapterTest {

    private S3Presigner s3Presigner;
    private S3VideoPlaybackUrlAdapter adapter;

    @BeforeEach
    void setUp() {
        s3Presigner = S3Presigner.builder()
                .region(Region.AP_NORTHEAST_2)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test-access-key", "test-secret-key")
                ))
                .build();
        S3UrlPresigner s3UrlPresigner = new S3UrlPresigner(s3Presigner);
        ReflectionTestUtils.setField(s3UrlPresigner, "bucket", "test-bucket");
        adapter = new S3VideoPlaybackUrlAdapter(10L, s3UrlPresigner);
    }

    @AfterEach
    void tearDown() {
        s3Presigner.close();
    }

    @Test
    void s3_key가_있으면_presigned_url을_발급한다() {
        String result = adapter.generatePlaybackUrl(accessInfo("videos/10.mp4", null));

        assertThat(result).contains("test-bucket");
        assertThat(result).contains("videos/10.mp4");
        assertThat(result).contains("X-Amz-Expires=600");
    }

    @Test
    void s3_key가_없으면_레거시_streamingUrl을_반환한다() {
        String result = adapter.generatePlaybackUrl(
                accessInfo(null, "https://stream.example.com/video.m3u8")
        );

        assertThat(result).isEqualTo("https://stream.example.com/video.m3u8");
    }

    @Test
    void s3_key와_레거시_url이_모두_없으면_예외가_발생한다() {
        assertThatThrownBy(() -> adapter.generatePlaybackUrl(accessInfo(null, null)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.VIDEO_PLAYBACK_URL_NOT_FOUND);
    }

    private VideoAccessInfo accessInfo(String s3Key, String streamingUrl) {
        return new VideoAccessInfo(
                10L,
                20L,
                "PUBLISHED",
                10000,
                false,
                s3Key,
                streamingUrl,
                300
        );
    }
}
