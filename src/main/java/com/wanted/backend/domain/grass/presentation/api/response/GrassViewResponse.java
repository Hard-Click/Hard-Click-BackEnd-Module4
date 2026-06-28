package com.wanted.backend.domain.grass.presentation.api.response;

import com.wanted.backend.domain.grass.application.usecase.GetGrassViewUseCase.GrassDayView;
import com.wanted.backend.domain.grass.application.usecase.GetGrassViewUseCase.GrassView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "잔디 보기 모드 전환 응답")
public record GrassViewResponse(
        @Schema(description = "보기 모드 (monthly / yearly)", example = "monthly")
        String view,

        @Schema(description = "연도", example = "2026")
        Integer year,

        @Schema(description = "월 (yearly 보기에서는 null)", example = "6")
        Integer month,

        @Schema(description = "날짜별 잔디 목록")
        List<GrassDayResponse> days
) {
    public static GrassViewResponse from(GrassView view) {
        return new GrassViewResponse(
                view.view(),
                view.year(),
                view.month(),
                view.days().stream().map(GrassDayResponse::from).toList()
        );
    }

    @Schema(description = "잔디 일자별 데이터")
    public record GrassDayResponse(
            @Schema(description = "날짜", example = "2026-06-01")
            LocalDate date,

            @Schema(description = "학습 값", example = "3")
            Integer value,

            @Schema(description = "잔디 레벨", example = "2")
            Integer level,

            @Schema(description = "미래 날짜 여부", example = "false")
            Boolean isFuture
    ) {
        public static GrassDayResponse from(GrassDayView view) {
            return new GrassDayResponse(
                    view.date(),
                    view.value(),
                    view.level(),
                    view.isFuture()
            );
        }
    }
}
