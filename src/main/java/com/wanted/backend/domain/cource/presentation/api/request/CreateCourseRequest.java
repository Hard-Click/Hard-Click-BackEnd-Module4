package com.wanted.backend.domain.cource.presentation.api.request;

import com.wanted.backend.domain.cource.application.command.CreateCourseCommand;
import com.wanted.backend.domain.cource.domain.model.PriceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public record CreateCourseRequest(
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

        @Valid
        List<SectionRequest> sections,

        List<String> learningObjectives,
        List<String> targetAudience,
        List<String> techTags,
        String level
) {
    public CreateCourseCommand toCommand(Long authorId) {
        List<CreateCourseCommand.SectionCommand> sectionCommands = sections == null
                ? new ArrayList<>()
                : sections.stream()
                        .map(s -> {
                            List<CreateCourseCommand.LessonCommand> lessonCommands = s.lessons().stream()
                                    .map(l -> new CreateCourseCommand.LessonCommand(
                                            l.title(), l.description(), l.orderIndex()))
                                    .toList();
                            return new CreateCourseCommand.SectionCommand(
                                    s.title(), s.orderIndex(), lessonCommands);
                        })
                        .toList();

        return new CreateCourseCommand(authorId, title, subject, description,
                thumbnailUrl, priceType, price, sectionCommands,
                learningObjectives, targetAudience, techTags, level);
    }
}
