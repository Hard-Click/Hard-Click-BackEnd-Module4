package com.wanted.backend.domain.ranking.presentation.api.response;

import com.wanted.backend.domain.ranking.application.usecase.GetMyRankingDetailUseCase.MyRankingDetailView;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 랭킹 상세 정보 응답")
public record MyRankingDetailResponse(
        @Schema(description = "조회 기준 지표", example = "studyTime")
        String metric,

        @Schema(description = "조회 기간", example = "weekly")
        String period,

        @Schema(description = "내 순위", example = "12")
        Long rank,

        @Schema(description = "전체 사용자 수", example = "1234")
        Long totalUsers,

        @Schema(description = "상위 퍼센트", example = "1.0")
        Double topPercent
) {
    public static MyRankingDetailResponse from(MyRankingDetailView view) {
        return new MyRankingDetailResponse(
                view.metric(),
                view.period(),
                view.rank(),
                view.totalUsers(),
                view.topPercent()
        );
    }
}
