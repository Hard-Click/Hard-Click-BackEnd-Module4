package com.wanted.backend.domain.learning_activity.infrastructure.playback;

import com.wanted.backend.domain.learning_activity.application.port.VideoPlaybackUrlPort;
import com.wanted.backend.domain.learning_activity.domain.model.VideoAccessInfo;
import com.wanted.backend.global.config.S3UrlPresigner;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class S3VideoPlaybackUrlAdapter implements VideoPlaybackUrlPort {

    private final long videoPresignedUrlMinutes;
    private final S3UrlPresigner s3UrlPresigner;

    public S3VideoPlaybackUrlAdapter(
            @Value("${aws.s3.presigned-url.video-minutes:10}") long videoPresignedUrlMinutes,
            S3UrlPresigner s3UrlPresigner
    ) {
        this.videoPresignedUrlMinutes = videoPresignedUrlMinutes;
        this.s3UrlPresigner = s3UrlPresigner;
    }

    @Override
    public String generatePlaybackUrl(VideoAccessInfo accessInfo) {
        if (accessInfo.hasS3Key()) {
            return presign(accessInfo.s3Key());
        }
        if (accessInfo.hasLegacyStreamingUrl()) {
            return accessInfo.streamingUrl();
        }
        throw new BusinessException(ErrorCode.VIDEO_PLAYBACK_URL_NOT_FOUND);
    }

    private String presign(String s3Key) {
        return s3UrlPresigner.presign(s3Key, Duration.ofMinutes(videoPresignedUrlMinutes));
    }
}
