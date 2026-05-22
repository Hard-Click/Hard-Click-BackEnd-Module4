package com.wanted.backend.domain.learning_activity.infrastructure.catalog;

import com.wanted.backend.domain.learning_activity.application.port.VideoCatalogPort;
import com.wanted.backend.domain.learning_activity.domain.model.VideoAccessInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VideoCatalogAdapter implements VideoCatalogPort {

    private final SpringDataVideoCatalogRepository repository;

    @Override
    public Optional<VideoAccessInfo> findByVideoId(Long videoId) {
        return repository.findByVideoId(videoId)
                .map(this::toDomain);
    }

    private VideoAccessInfo toDomain(SpringDataVideoCatalogRepository.VideoAccessProjection projection) {
        return new VideoAccessInfo(
                projection.getVideoId(),
                projection.getCourseId(),
                projection.getCourseStatus(),
                projection.getCoursePrice(),
                projection.getPreview() != null && projection.getPreview() == 1,
                projection.getStreamingUrl(),
                projection.getDurationSeconds()
        );
    }
}
