package com.wanted.backend.domain.learning_activity.infrastructure.s3;

import com.wanted.backend.domain.learning_activity.application.port.VideoPlayUrlPort;
import com.wanted.backend.global.config.S3UrlPresigner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class S3VideoPlayUrlAdapter implements VideoPlayUrlPort {

    private final S3UrlPresigner s3UrlPresigner;

    @Override
    public String generateUrl(String s3Key) {
        return s3UrlPresigner.presign(s3Key);
    }
}
