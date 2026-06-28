package com.wanted.backend.domain.grass.presentation.api.response;

import com.wanted.backend.domain.grass.application.usecase.GetLessonGrassUseCase.LessonGrassView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "수강량 잔디 응답")
public record LessonGrassResponse(
        @Schema(description = "날짜", example = "2026-06-01")
        LocalDate date,

        @Schema(description = "해당 날짜 수강 완료한 레슨 수", example = "2")
        Integer watchedLessonCount,

        @Schema(description = "잔디 레벨", example = "2")
        Integer level,

        @Schema(description = "미래 날짜 여부", example = "false")
        Boolean isFuture
) {
    public static LessonGrassResponse from(LessonGrassView view) {
        return new LessonGrassResponse(
                view.date(),
                view.watchedLessonCount(),
                view.level(),
                view.isFuture()
        );
    }
}
