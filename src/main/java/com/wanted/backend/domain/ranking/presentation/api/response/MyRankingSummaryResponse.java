package com.wanted.backend.domain.ranking.presentation.api.response;

import com.wanted.backend.domain.ranking.application.usecase.GetMyRankingSummaryUseCase.MyRankingSummaryView;
import com.wanted.backend.domain.ranking.application.usecase.GetMyRankingSummaryUseCase.RankingSummaryItem;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 랭킹 요약 정보 응답")
public record MyRankingSummaryResponse(
        @Schema(description = "순공시간 랭킹 요약")
        RankingSummaryItemResponse studyTime,

        @Schema(description = "수강량 랭킹 요약")
        RankingSummaryItemResponse lesson,

        @Schema(description = "댓글 채택 수 랭킹 요약")
        RankingSummaryItemResponse acceptedComment
) {
    public static MyRankingSummaryResponse from(MyRankingSummaryView view) {
        return new MyRankingSummaryResponse(
                RankingSummaryItemResponse.from(view.studyTime()),
                RankingSummaryItemResponse.from(view.lesson()),
                RankingSummaryItemResponse.from(view.acceptedComment())
        );
    }

    @Schema(description = "랭킹 요약 항목")
    public record RankingSummaryItemResponse(
            @Schema(description = "내 순위", example = "12")
            Long rank,

            @Schema(description = "전체 사용자 수", example = "1234")
            Long totalUsers,

            @Schema(description = "상위 퍼센트", example = "1.0")
            Double topPercent
    ) {
        public static RankingSummaryItemResponse from(RankingSummaryItem item) {
            return new RankingSummaryItemResponse(
                    item.rank(),
                    item.totalUsers(),
                    item.topPercent()
            );
        }
    }
}
