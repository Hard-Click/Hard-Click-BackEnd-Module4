package com.wanted.backend.domain.community.presentation;

import com.wanted.backend.domain.community.application.command.CreateReviewCommand;
import com.wanted.backend.domain.community.application.command.DeleteReviewCommand;
import com.wanted.backend.domain.community.application.command.UpdateReviewCommand;
import com.wanted.backend.domain.community.application.result.ReviewListResult;
import com.wanted.backend.domain.community.application.usecase.ReviewCommandUseCase;
import com.wanted.backend.domain.community.application.usecase.ReviewQueryUseCase;
import com.wanted.backend.domain.community.domain.model.ReviewSortType;
import com.wanted.backend.domain.community.presentation.request.CreateReviewRequest;
import com.wanted.backend.domain.community.presentation.request.UpdateReviewRequest;
import com.wanted.backend.domain.community.presentation.response.CreateReviewResponse;
import com.wanted.backend.domain.community.presentation.response.ReviewListResponse;
import com.wanted.backend.domain.community.presentation.response.UpdateReviewResponse;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Review", description = "강의 리뷰 API")
@RestController
@RequestMapping("/api/courses/{courseId}/reviews")
public class ReviewController {

    private final ReviewCommandUseCase reviewCommandUseCase;
    private final ReviewQueryUseCase reviewQueryUseCase;

    public ReviewController(ReviewCommandUseCase reviewCommandUseCase,
                            ReviewQueryUseCase reviewQueryUseCase) {
        this.reviewCommandUseCase = reviewCommandUseCase;
        this.reviewQueryUseCase = reviewQueryUseCase;
    }

    @PostMapping
    @Operation(
            summary = "리뷰 등록",
            description = """
                강의를 시청한 회원이 해당 강의에 리뷰를 등록합니다.
                - 강의당 리뷰는 1개만 등록 가능합니다.
                - 별점은 1~5 사이의 정수만 허용합니다.
                - 리뷰 내용은 10자 이상 300자 이하여야 합니다.
                - 로그인한 회원만 등록할 수 있습니다.
                """
    )
    public ResponseEntity<ApiResponse<CreateReviewResponse>> createReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long courseId,
            @Valid @RequestBody CreateReviewRequest request) {

        Long reviewId = reviewCommandUseCase.handle(new CreateReviewCommand(
                userDetails.getMemberId(),
                courseId,
                request.rating(),
                request.content()
        ));

        return ApiResponse.created("리뷰가 등록되었습니다.", new CreateReviewResponse(reviewId));
    }

    @GetMapping
    @Operation(
            summary = "리뷰 목록 조회",
            description = """
                강의의 리뷰 목록을 조회합니다.
                - 비회원도 조회 가능합니다.
                - 정렬 기준: latest(최신순), rating(별점순) — 기본값: latest
                - 페이지 기본값: 0
                - 조회 시 별점 평균 및 별점 분포(1~5점)를 함께 반환합니다.
                - 본인이 작성한 리뷰는 isMyReview: true로 표시됩니다(최상단 정렬).
                """
    )
    public ResponseEntity<ApiResponse<ReviewListResponse>> getReviews(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "latest") ReviewSortType sort,
            @RequestParam(defaultValue = "0") int page) {

        Long currentMemberId = userDetails != null ? userDetails.getMemberId() : -1L;
        ReviewListResult result = reviewQueryUseCase.handle(courseId, currentMemberId, sort, page);
        return ApiResponse.success("리뷰 목록 조회 성공", ReviewListResponse.from(result));
    }


    @PatchMapping("/{reviewId}")
    @Operation(
            summary = "리뷰 수정",
            description = """
                본인이 작성한 리뷰를 수정합니다.
                - 본인이 작성한 리뷰인지 검증 후 수정합니다.
                - 별점은 1~5 사이의 정수만 허용합니다.
                - 리뷰 내용은 10자 이상 300자 이하여야 합니다.
                - 로그인한 회원만 수정할 수 있습니다.
                """
    )
    public ResponseEntity<ApiResponse<UpdateReviewResponse>> updateReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewRequest request) {

        Long updatedReviewId = reviewCommandUseCase.update(new UpdateReviewCommand(
                userDetails.getMemberId(),
                reviewId,
                request.rating(),
                request.content()
        ));

        return ApiResponse.success("리뷰가 수정되었습니다.", new UpdateReviewResponse(updatedReviewId));
    }

    @DeleteMapping("/{reviewId}")
    @Operation(
            summary = "리뷰 삭제",
            description = """
                본인이 작성한 리뷰를 삭제합니다.
                - 본인이 작성한 리뷰인지 검증 후 삭제합니다.
                - 로그인한 회원만 삭제할 수 있습니다.
                """
    )
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reviewId) {

        reviewCommandUseCase.delete(new DeleteReviewCommand(
                userDetails.getMemberId(), reviewId,
                "ADMIN".equals(userDetails.getRole())));

        return ApiResponse.successNoContent("리뷰가 삭제되었습니다.");
    }
}