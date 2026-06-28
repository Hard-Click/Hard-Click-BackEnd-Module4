package com.wanted.backend.domain.grass.presentation.api.response;

import com.wanted.backend.domain.grass.application.usecase.GetYearlyGrassUseCase.YearlyGrassDayView;
import com.wanted.backend.domain.grass.application.usecase.GetYearlyGrassUseCase.YearlyGrassView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "연간 잔디 응답")
public record YearlyGrassResponse(
        @Schema(description = "연도", example = "2026")
        Integer year,

        @Schema(description = "날짜별 잔디 목록")
        List<YearlyGrassDayResponse> days
) {
    public static YearlyGrassResponse from(YearlyGrassView view) {
        return new YearlyGrassResponse(
                view.year(),
                view.days().stream().map(YearlyGrassDayResponse::from).toList()
        );
    }

    @Schema(description = "연간 잔디 일자별 데이터")
    public record YearlyGrassDayResponse(
            @Schema(description = "날짜", example = "2026-06-01")
            LocalDate date,

            @Schema(description = "학습 값", example = "3")
            Integer value,

            @Schema(description = "잔디 레벨", example = "2")
            Integer level,

            @Schema(description = "미래 날짜 여부", example = "false")
            Boolean isFuture
    ) {
        public static YearlyGrassDayResponse from(YearlyGrassDayView view) {
            return new YearlyGrassDayResponse(
                    view.date(),
                    view.value(),
                    view.level(),
                    view.isFuture()
            );
        }
    }
}
