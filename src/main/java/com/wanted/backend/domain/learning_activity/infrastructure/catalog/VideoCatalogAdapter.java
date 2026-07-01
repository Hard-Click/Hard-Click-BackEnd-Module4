package com.wanted.backend.domain.learning_activity.infrastructure.catalog;

import com.wanted.backend.domain.learning_activity.application.port.VideoCatalogPort;
import com.wanted.backend.domain.learning_activity.application.port.VideoPlayUrlPort;
import com.wanted.backend.domain.learning_activity.domain.model.VideoAccessInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VideoCatalogAdapter implements VideoCatalogPort {

    private final LessonReferenceRepository lessonRepository;
    private final CourseSectionReferenceRepository sectionRepository;
    private final CatalogCourseReferenceRepository courseRepository;
    private final VideoPlayUrlPort videoPlayUrlPort;

    @Override
    public Optional<VideoAccessInfo> findByVideoId(Long videoId) {
        // 재생 영상의 식별자(videoId)는 이제 lesson.id다 — lesson -> course_section -> course 순으로
        // 필요한 참조 정보를 조합한다. 레포지토리는 단순 조회만, 조합 로직은 어댑터에 둔다.
        return lessonRepository.findById(videoId)
                .flatMap(this::toDomain);
    }

    private Optional<VideoAccessInfo> toDomain(LessonReferenceEntity lesson) {
        return sectionRepository.findById(lesson.getSectionId())
                .flatMap(section -> courseRepository.findById(section.getCourseId())
                        .map(course -> toDomain(lesson, section, course)));
    }

    private VideoAccessInfo toDomain(
            LessonReferenceEntity lesson,
            CourseSectionReferenceEntity section,
            CatalogCourseReferenceEntity course
    ) {
        // 강의 상세 조회(CourseQueryService)와 동일한 휴리스틱: 첫 섹션의 첫 레슨을 미리보기로 취급한다.
        boolean isPreview = PreviewLessonPolicy.isPreview(section, lesson);

        // s3Key가 있으면 presigned GET URL을 실시간 발급(만료 문제 해소).
        // s3Key가 없는 레거시 레슨은 기존에 저장된 video_url을 그대로 폴백으로 사용한다.
        String streamingUrl = (lesson.getS3Key() != null && !lesson.getS3Key().isBlank())
                ? videoPlayUrlPort.generateUrl(lesson.getS3Key())
                : lesson.getVideoUrl();

        return new VideoAccessInfo(
                lesson.getId(),
                section.getCourseId(),
                course.getStatus(),
                course.getPrice(),
                isPreview,
                lesson.getS3Key(),
                streamingUrl,
                lesson.getDurationSeconds()
        );
    }
}
