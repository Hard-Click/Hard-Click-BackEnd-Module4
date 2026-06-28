package com.wanted.backend.domain.grass.presentation.api.response;

import com.wanted.backend.domain.grass.application.usecase.GetDailyGrassDetailUseCase.DailyGrassDetailView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "특정 날짜 잔디 상세 응답")
public record DailyGrassDetailResponse(
        @Schema(description = "날짜", example = "2026-06-01")
        LocalDate date,

        @Schema(description = "해당 날짜 수강 완료한 레슨 수", example = "2")
        Integer watchedLessonCount,

        @Schema(description = "해당 날짜 순공시간 (초)", example = "3600")
        Integer studySeconds,

        @Schema(description = "학습 기록 존재 여부", example = "true")
        Boolean hasStudyRecord
) {
    public static DailyGrassDetailResponse from(DailyGrassDetailView view) {
        return new DailyGrassDetailResponse(
                view.date(),
                view.watchedLessonCount(),
                view.studySeconds(),
                view.hasStudyRecord()
        );
    }
}
