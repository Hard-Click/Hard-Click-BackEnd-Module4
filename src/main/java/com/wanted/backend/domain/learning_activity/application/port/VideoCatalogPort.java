package com.wanted.backend.domain.learning_activity.application.port;

import com.wanted.backend.domain.learning_activity.domain.model.VideoAccessInfo;

import java.util.Optional;

public interface VideoCatalogPort {

    // 콘텐츠/강의 컨텍스트에서 재생에 필요한 영상 정보를 가져오는 포트
    Optional<VideoAccessInfo> findByVideoId(Long videoId);
}
