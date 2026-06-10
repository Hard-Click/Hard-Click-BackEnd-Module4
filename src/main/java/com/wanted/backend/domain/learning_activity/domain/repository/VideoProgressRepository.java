package com.wanted.backend.domain.learning_activity.domain.repository;

import com.wanted.backend.domain.learning_activity.domain.model.VideoProgress;

import java.util.Optional;

public interface VideoProgressRepository {

    Optional<VideoProgress> findByMemberIdAndVideoId(Long memberId, Long videoId);

    VideoProgress save(VideoProgress progress);
}
