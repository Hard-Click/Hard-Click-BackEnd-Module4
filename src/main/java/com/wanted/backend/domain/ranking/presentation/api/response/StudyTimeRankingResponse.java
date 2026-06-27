package com.wanted.backend.domain.ranking.presentation.api.response;

import com.wanted.backend.domain.ranking.application.usecase.GetStudyTimeRankingUseCase.StudyTimeRankingItem;
import com.wanted.backend.domain.ranking.application.usecase.GetStudyTimeRankingUseCase.StudyTimeRankingView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "순공시간 랭킹 응답")
public record StudyTimeRankingResponse(
        @Schema(description = "조회 기간", example = "weekly")
        String period,

        @Schema(description = "전체 사용자 수", example = "1234")
        Long totalUsers,

        @Schema(description = "랭킹 목록")
        List<StudyTimeRankingItemResponse> rankings
) {
    public static StudyTimeRankingResponse from(StudyTimeRankingView view) {
        return new StudyTimeRankingResponse(
                view.period(),
                view.totalUsers(),
                view.rankings().stream().map(StudyTimeRankingItemResponse::from).toList()
        );
    }

    @Schema(description = "순공시간 랭킹 항목")
    public record StudyTimeRankingItemResponse(
            @Schema(description = "순위", example = "1")
            Long rank,

            @Schema(description = "회원 ID", example = "10")
            Long memberId,

            @Schema(description = "회원 이름", example = "박지훈")
            String memberName,

            @Schema(description = "순공시간 (초)", example = "36000")
            Long studySeconds,

            @Schema(description = "현재 연속 학습일", example = "5")
            Integer currentStreakDays
    ) {
        public static StudyTimeRankingItemResponse from(StudyTimeRankingItem item) {
            return new StudyTimeRankingItemResponse(
                    item.rank(),
                    item.memberId(),
                    item.memberName(),
                    item.studySeconds(),
                    item.currentStreakDays()
            );
        }
    }
}
