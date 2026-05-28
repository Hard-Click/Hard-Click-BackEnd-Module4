package com.wanted.backend.domain.cource.presentation.api.response;

import com.wanted.backend.domain.cource.application.dto.CourseDetailResult;
import com.wanted.backend.domain.cource.domain.model.CourseStatus;
import com.wanted.backend.domain.cource.domain.model.PriceType;

import java.util.List;

public record CourseDetailResponse(
        Long courseId,
        String title,
        String subjectName,
        String description,
        String thumbnailUrl,
        PriceType priceType,
        int price,
        String priceLabel,
        CourseStatus status,
        String instructorName,
        double averageRating,
        int reviewCount,
        int studentCount,
        List<SectionResponse> sections,
        List<String> learningObjectives,
        List<String> targetAudience,
        List<String> techTags,
        String level
) {
    public record SectionResponse(
            Long sectionId,
            String title,
            int orderIndex,
            List<LessonResponse> lessons
    ) {}

    public record LessonResponse(
            Long lessonId,
            String title,
            String description,
            int orderIndex,
            Integer durationSeconds
    ) {}

    public static CourseDetailResponse from(CourseDetailResult result) {
        String priceLabel = result.priceType() == PriceType.FREE
                ? "무료"
                : String.format("%,d원", result.price());

        List<SectionResponse> sections = result.sections().stream()
                .map(s -> new SectionResponse(
                        s.sectionId(),
                        s.title(),
                        s.orderIndex(),
                        s.lessons().stream()
                                .map(l -> new LessonResponse(
                                        l.lessonId(),
                                        l.title(),
                                        l.description(),
                                        l.orderIndex(),
                                        l.durationSeconds()
                                ))
                                .toList()
                ))
                .toList();

        return new CourseDetailResponse(
                result.courseId(),
                result.title(),
                result.subject(),
                result.description(),
                result.thumbnailUrl(),
                result.priceType(),
                result.price(),
                priceLabel,
                result.status(),
                result.instructorName(),
                result.rating(),
                result.reviewCount(),
                result.studentCount(),
                sections,
                result.learningObjectives(),
                result.targetAudience(),
                result.techTags(),
                result.level()
        );
    }
}
