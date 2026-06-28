package com.wanted.backend.domain.grass.presentation.api.response;

import com.wanted.backend.domain.grass.application.usecase.GetStudyStreakUseCase.StudyStreakView;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "연속 학습일 응답")
public record StudyStreakResponse(
        @Schema(description = "오늘 기준 연속 학습일", example = "5")
        Integer streak
) {
    public static StudyStreakResponse from(StudyStreakView view) {
        return new StudyStreakResponse(view.streak());
    }
}
