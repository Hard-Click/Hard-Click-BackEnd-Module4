package com.wanted.backend.domain.learning_activity.infrastructure.catalog;

import com.wanted.backend.domain.learning_activity.infrastructure.persistence.VideoProgressJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataVideoCatalogRepository extends JpaRepository<VideoProgressJpaEntity, Long> {

    @Query(value = """
            SELECT v.video_id AS videoId,
                   cc.course_id AS courseId,
                   c.status AS courseStatus,
                   c.price AS coursePrice,
                   v.is_preview AS preview,
                   v.video_url AS streamingUrl,
                   v.duration_seconds AS durationSeconds
            FROM videos v
            JOIN course_curriculum cc ON cc.curriculum_id = v.curriculum_id
            JOIN courses c ON c.course_id = cc.course_id
            WHERE v.video_id = :videoId
            """, nativeQuery = true)
    Optional<VideoAccessProjection> findByVideoId(@Param("videoId") Long videoId);

    interface VideoAccessProjection {
        Long getVideoId();

        Long getCourseId();

        String getCourseStatus();

        Integer getCoursePrice();

        Byte getPreview();

        String getStreamingUrl();

        Integer getDurationSeconds();
    }
}
