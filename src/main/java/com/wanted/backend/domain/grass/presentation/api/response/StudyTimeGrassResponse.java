package com.wanted.backend.domain.grass.presentation.api.response;

import com.wanted.backend.domain.grass.application.usecase.GetStudyTimeGrassUseCase.StudyTimeGrassView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "순공시간 잔디 응답")
public record StudyTimeGrassResponse(
        @Schema(description = "날짜", example = "2026-06-01")
        LocalDate date,

        @Schema(description = "해당 날짜 순공시간 (초)", example = "3600")
        Integer studySeconds,

        @Schema(description = "잔디 레벨", example = "2")
        Integer level,

        @Schema(description = "미래 날짜 여부", example = "false")
        Boolean isFuture
) {
    public static StudyTimeGrassResponse from(StudyTimeGrassView view) {
        return new StudyTimeGrassResponse(
                view.date(),
                view.studySeconds(),
                view.level(),
                view.isFuture()
        );
    }
}
