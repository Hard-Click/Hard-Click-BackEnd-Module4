package com.wanted.backend.domain.learning_activity.infrastructure.persistence;

import com.wanted.backend.domain.learning_activity.domain.model.VideoProgress;
import com.wanted.backend.domain.learning_activity.domain.repository.VideoProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VideoProgressRepositoryAdapter implements VideoProgressRepository {

    private final SpringDataVideoProgressRepository repository;

    @Override
    public Optional<VideoProgress> findByMemberIdAndVideoId(Long memberId, Long videoId) {
        return repository.findByMemberIdAndVideoId(memberId, videoId)
                .map(this::toDomain);
    }

    private VideoProgress toDomain(VideoProgressJpaEntity entity) {
        return new VideoProgress(
                entity.getId(),
                entity.getMemberId(),
                entity.getCourseId(),
                entity.getVideoId(),
                entity.getLastPositionSec(),
                entity.getWatchTimeSec(),
                entity.getCompleted(),
                entity.getCompletedAt()
        );
    }
}
