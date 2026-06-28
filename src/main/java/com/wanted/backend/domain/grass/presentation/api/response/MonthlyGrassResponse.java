package com.wanted.backend.domain.grass.presentation.api.response;

import com.wanted.backend.domain.grass.application.usecase.GetMonthlyGrassUseCase.MonthlyGrassDayView;
import com.wanted.backend.domain.grass.application.usecase.GetMonthlyGrassUseCase.MonthlyGrassView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "월별 잔디 응답")
public record MonthlyGrassResponse(
        @Schema(description = "연도", example = "2026")
        Integer year,

        @Schema(description = "월", example = "6")
        Integer month,

        @Schema(description = "날짜별 잔디 목록")
        List<MonthlyGrassDayResponse> days
) {
    public static MonthlyGrassResponse from(MonthlyGrassView view) {
        return new MonthlyGrassResponse(
                view.year(),
                view.month(),
                view.days().stream().map(MonthlyGrassDayResponse::from).toList()
        );
    }

    @Schema(description = "월별 잔디 일자별 데이터")
    public record MonthlyGrassDayResponse(
            @Schema(description = "날짜", example = "2026-06-01")
            LocalDate date,

            @Schema(description = "학습 값", example = "3")
            Integer value,

            @Schema(description = "잔디 레벨", example = "2")
            Integer level,

            @Schema(description = "미래 날짜 여부", example = "false")
            Boolean isFuture
    ) {
        public static MonthlyGrassDayResponse from(MonthlyGrassDayView view) {
            return new MonthlyGrassDayResponse(
                    view.date(),
                    view.value(),
                    view.level(),
                    view.isFuture()
            );
        }
    }
}
