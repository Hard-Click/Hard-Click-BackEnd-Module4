package com.wanted.backend.domain.ranking.presentation.api.response;

import com.wanted.backend.domain.ranking.application.usecase.GetLessonRankingUseCase.LessonRankingItem;
import com.wanted.backend.domain.ranking.application.usecase.GetLessonRankingUseCase.LessonRankingView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "수강량 랭킹 응답")
public record LessonRankingResponse(
        @Schema(description = "조회 기간", example = "weekly")
        String period,

        @Schema(description = "전체 사용자 수", example = "1234")
        Long totalUsers,

        @Schema(description = "랭킹 목록")
        List<LessonRankingItemResponse> rankings
) {
    public static LessonRankingResponse from(LessonRankingView view) {
        return new LessonRankingResponse(
                view.period(),
                view.totalUsers(),
                view.rankings().stream().map(LessonRankingItemResponse::from).toList()
        );
    }

    @Schema(description = "수강량 랭킹 항목")
    public record LessonRankingItemResponse(
            @Schema(description = "순위", example = "1")
            Long rank,

            @Schema(description = "회원 ID", example = "10")
            Long memberId,

            @Schema(description = "회원 이름", example = "박지훈")
            String memberName,

            @Schema(description = "수강 완료한 레슨 수", example = "42")
            Long watchedLessonCount,

            @Schema(description = "현재 연속 학습일", example = "5")
            Integer currentStreakDays
    ) {
        public static LessonRankingItemResponse from(LessonRankingItem item) {
            return new LessonRankingItemResponse(
                    item.rank(),
                    item.memberId(),
                    item.memberName(),
                    item.watchedLessonCount(),
                    item.currentStreakDays()
            );
        }
    }
}
