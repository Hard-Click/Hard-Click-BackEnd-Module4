package com.wanted.backend.domain.learning_activity.application.port;

import com.wanted.backend.domain.learning_activity.domain.model.VideoAccessInfo;

public interface VideoPlaybackUrlPort {

    String generatePlaybackUrl(VideoAccessInfo accessInfo);
}
