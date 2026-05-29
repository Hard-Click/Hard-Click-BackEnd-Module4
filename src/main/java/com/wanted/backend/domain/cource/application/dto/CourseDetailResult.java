package com.wanted.backend.domain.cource.application.dto;

import com.wanted.backend.domain.cource.domain.model.CourseStatus;
import com.wanted.backend.domain.cource.domain.model.PriceType;

import java.util.List;

public record CourseDetailResult(
        Long courseId,
        String title,
        String subject,
        String description,
        String thumbnailUrl,
        PriceType priceType,
        int price,
        CourseStatus status,
        String instructorName,
        double rating,
        int reviewCount,
        int studentCount,
        List<SectionResult> sections,
        List<String> learningObjectives,
        List<String> targetAudience,
        List<String> techTags,
        String level
) {
    public record SectionResult(
            Long sectionId,
            String title,
            int orderIndex,
            List<LessonResult> lessons
    ) {}

    public record LessonResult(
            Long lessonId,
            String title,
            String description,
            int orderIndex,
            Integer durationSeconds,
            boolean isPreview
    ) {}
}
