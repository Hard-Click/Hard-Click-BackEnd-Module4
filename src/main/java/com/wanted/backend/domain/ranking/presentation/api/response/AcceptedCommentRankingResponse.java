package com.wanted.backend.domain.ranking.presentation.api.response;

import com.wanted.backend.domain.ranking.application.usecase.GetAcceptedCommentRankingUseCase.AcceptedCommentRankingItem;
import com.wanted.backend.domain.ranking.application.usecase.GetAcceptedCommentRankingUseCase.AcceptedCommentRankingView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "댓글 채택 수 랭킹 응답")
public record AcceptedCommentRankingResponse(
        @Schema(description = "조회 기간", example = "weekly")
        String period,

        @Schema(description = "전체 사용자 수", example = "1234")
        Long totalUsers,

        @Schema(description = "랭킹 목록")
        List<AcceptedCommentRankingItemResponse> rankings
) {
    public static AcceptedCommentRankingResponse from(AcceptedCommentRankingView view) {
        return new AcceptedCommentRankingResponse(
                view.period(),
                view.totalUsers(),
                view.rankings().stream().map(AcceptedCommentRankingItemResponse::from).toList()
        );
    }

    @Schema(description = "댓글 채택 수 랭킹 항목")
    public record AcceptedCommentRankingItemResponse(
            @Schema(description = "순위", example = "1")
            Long rank,

            @Schema(description = "회원 ID", example = "10")
            Long memberId,

            @Schema(description = "회원 이름", example = "박지훈")
            String memberName,

            @Schema(description = "채택된 댓글 수", example = "7")
            Long acceptedCommentCount,

            @Schema(description = "현재 연속 학습일", example = "5")
            Integer currentStreakDays
    ) {
        public static AcceptedCommentRankingItemResponse from(AcceptedCommentRankingItem item) {
            return new AcceptedCommentRankingItemResponse(
                    item.rank(),
                    item.memberId(),
                    item.memberName(),
                    item.acceptedCommentCount(),
                    item.currentStreakDays()
            );
        }
    }
}
