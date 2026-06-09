package com.wanted.backend.domain.cource.presentation.api.request;

import com.wanted.backend.domain.cource.application.command.UpdateCourseCommand;
import com.wanted.backend.domain.cource.domain.model.PriceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public record UpdateCourseRequest(
        @NotBlank(message = "강의명은 필수입니다.")
        String title,

        @NotBlank(message = "과목은 필수입니다.")
        String subject,

        @NotBlank(message = "강의 설명은 필수입니다.")
        String description,

        String thumbnailUrl,

        @NotNull(message = "가격 유형은 필수입니다.")
        PriceType priceType,

        @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
        int price,

        `@NotNull`(message = "커리큘럼(sections)은 필수입니다. 전체 삭제가 필요하면 빈 배열([])을 전달하세요.")
        `@Valid`
        List<UpdateSectionRequest> sections,
        List<UpdateSectionRequest> sections,

        List<String> learningObjectives,
        List<String> targetAudience,
        List<String> techTags,
        String level
) {
    public UpdateCourseCommand toCommand(Long courseId, Long requesterId) {
        List<UpdateCourseCommand.SectionCommand> sectionCommands = sections == null
                ? new ArrayList<>()
                : sections.stream()
                        .map(s -> {
                            List<UpdateCourseCommand.LessonCommand> lessonCommands = s.lessons() == null
                                    ? new ArrayList<>()
                                    : s.lessons().stream()
                                            .map(l -> new UpdateCourseCommand.LessonCommand(
                                                    l.id(), l.title(), l.description(), l.orderIndex(), l.durationSeconds()))
                                            .toList();
                            return new UpdateCourseCommand.SectionCommand(
                                    s.id(), s.title(), s.orderIndex(), lessonCommands);
                        })
                        .toList();

        return new UpdateCourseCommand(courseId, requesterId, title, subject, description,
                thumbnailUrl, priceType, price, sectionCommands,
                learningObjectives, targetAudience, techTags, level);
    }
}
