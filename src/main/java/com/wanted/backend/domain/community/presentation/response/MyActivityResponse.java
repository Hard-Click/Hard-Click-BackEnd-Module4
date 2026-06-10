package com.wanted.backend.domain.community.presentation.response;

import com.wanted.backend.domain.community.application.usecase.GetMyActivityUseCase;
import com.wanted.backend.domain.community.domain.model.BoardType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record MyActivityResponse(
        @Schema(description = "내가 작성한 게시글 목록")
        List<MyPostActivityResponse> posts,

        @Schema(description = "내가 작성한 댓글 목록")
        List<MyCommentActivityResponse> comments,

        @Schema(description = "내가 작성한 리뷰 목록")
        List<MyReviewActivityResponse> reviews
) {

    public static MyActivityResponse from(GetMyActivityUseCase.MyActivityView view) {
        return new MyActivityResponse(
                view.posts().stream().map(MyPostActivityResponse::from).toList(),
                view.comments().stream().map(MyCommentActivityResponse::from).toList(),
                view.reviews().stream().map(MyReviewActivityResponse::from).toList()
        );
    }

    public record MyPostActivityResponse(
            @Schema(description = "게시글 ID", example = "100")
            Long postId,
            @Schema(description = "게시판 유형", example = "QNA")
            BoardType boardType,
            @Schema(description = "게시글 제목", example = "Spring 질문입니다")
            String title,
            @Schema(description = "조회수", example = "12")
            Integer viewCount,
            @Schema(description = "채택 여부", example = "false")
            Boolean accepted,
            @Schema(description = "작성일시", example = "2026-05-28T10:30:00")
            LocalDateTime createdAt
    ) {
        private static MyPostActivityResponse from(GetMyActivityUseCase.MyPostActivity activity) {
            return new MyPostActivityResponse(
                    activity.postId(),
                    activity.boardType(),
                    activity.title(),
                    activity.viewCount(),
                    activity.accepted(),
                    activity.createdAt()
            );
        }
    }

    public record MyCommentActivityResponse(
            @Schema(description = "댓글 ID", example = "200")
            Long commentId,
            @Schema(description = "게시글 ID", example = "100")
            Long postId,
            @Schema(description = "부모 댓글 ID", example = "10")
            Long parentId,
            @Schema(description = "댓글 내용", example = "저도 같은 문제가 있었습니다.")
            String content,
            @Schema(description = "채택 여부", example = "true")
            Boolean accepted,
            @Schema(description = "작성일시", example = "2026-05-28T11:00:00")
            LocalDateTime createdAt
    ) {
        private static MyCommentActivityResponse from(GetMyActivityUseCase.MyCommentActivity activity) {
            return new MyCommentActivityResponse(
                    activity.commentId(),
                    activity.postId(),
                    activity.parentId(),
                    activity.content(),
                    activity.accepted(),
                    activity.createdAt()
            );
        }
    }

    public record MyReviewActivityResponse(
            @Schema(description = "리뷰 ID", example = "300")
            Long reviewId,
            @Schema(description = "강의 ID", example = "20")
            Long courseId,
            @Schema(description = "평점", example = "5")
            Integer rating,
            @Schema(description = "리뷰 내용", example = "강의가 이해하기 쉬웠습니다.")
            String content,
            @Schema(description = "작성일시", example = "2026-05-28T12:00:00")
            LocalDateTime createdAt
    ) {
        private static MyReviewActivityResponse from(GetMyActivityUseCase.MyReviewActivity activity) {
            return new MyReviewActivityResponse(
                    activity.reviewId(),
                    activity.courseId(),
                    activity.rating(),
                    activity.content(),
                    activity.createdAt()
            );
        }
    }
}
