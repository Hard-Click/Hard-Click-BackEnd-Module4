package com.wanted.backend.domain.learning_activity.application.port;

public interface VideoPlayUrlPort {
    String generateUrl(String s3Key);
}
