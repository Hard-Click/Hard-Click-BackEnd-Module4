package com.wanted.backend.domain.learning_activity.infrastructure.catalog;

import com.wanted.backend.domain.learning_activity.application.port.VideoCatalogPort;
import com.wanted.backend.domain.learning_activity.application.port.VideoPlayUrlPort;
import com.wanted.backend.domain.learning_activity.domain.model.VideoAccessInfo;
import com.wanted.backend.domain.learning_activity.infrastructure.curriculum.CourseCurriculumReferenceEntity;
import com.wanted.backend.domain.learning_activity.infrastructure.curriculum.CourseCurriculumReferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VideoCatalogAdapter implements VideoCatalogPort {

    private final CatalogVideoReferenceRepository videoRepository;
    private final CourseCurriculumReferenceRepository curriculumRepository;
    private final CatalogCourseReferenceRepository courseRepository;
    private final VideoPlayUrlPort videoPlayUrlPort;

    @Override
    public Optional<VideoAccessInfo> findByVideoId(Long videoId) {
        return videoRepository.findById(videoId)
                .flatMap(this::toDomain);
    }

    private Optional<VideoAccessInfo> toDomain(CatalogVideoReferenceEntity video) {
        // videos -> course_curriculum -> courses 순서로 필요한 참조 정보를 조합합니다.
        // 레포지토리는 단순 조회만 담당하고, 조합 로직은 어댑터에 둡니다.
        return curriculumRepository.findById(video.getCurriculumId())
                .flatMap(curriculum -> courseRepository.findById(curriculum.getCourseId())
                        .map(course -> toDomain(video, curriculum, course)));
    }

    private VideoAccessInfo toDomain(
            CatalogVideoReferenceEntity video,
            CourseCurriculumReferenceEntity curriculum,
            CatalogCourseReferenceEntity course
    ) {
        // 첫 번째 섹션(order_index=0)의 첫 번째 강의(order_index=0)를 무료 미리보기로 처리
        boolean isPreview = Integer.valueOf(0).equals(video.getOrderIndex())
                && Integer.valueOf(0).equals(curriculum.getOrderIndex());

        String streamingUrl = video.getStreamingUrl() != null
                ? videoPlayUrlPort.generateUrl(video.getStreamingUrl())
                : null;

        return new VideoAccessInfo(
                video.getId(),
                curriculum.getCourseId(),
                course.getStatus(),
                course.getPrice(),
                isPreview,
                streamingUrl,
                video.getDurationSeconds()
        );
    }
}
