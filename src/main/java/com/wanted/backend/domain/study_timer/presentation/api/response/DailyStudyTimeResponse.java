package com.wanted.backend.domain.study_timer.presentation.api.response;

import com.wanted.backend.domain.study_timer.application.usecase.GetDailyStudyTimeUseCase.DailyStudyTimeItem;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "일별 순공시간 응답")
public record DailyStudyTimeResponse(
        @Schema(description = "날짜", example = "2026-05-01")
        LocalDate date,

        @Schema(description = "해당 날짜 순공시간 합계 (초)", example = "120")
        Integer studySeconds
) {
    public static DailyStudyTimeResponse from(DailyStudyTimeItem item) {
        return new DailyStudyTimeResponse(
                item.date(),
                item.studySeconds()
        );
    }
}
