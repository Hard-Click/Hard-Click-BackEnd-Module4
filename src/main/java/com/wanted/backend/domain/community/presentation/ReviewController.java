package com.wanted.backend.domain.community.presentation;

import com.wanted.backend.domain.community.application.command.CreateReviewCommand;
import com.wanted.backend.domain.community.application.command.DeleteReviewCommand;
import com.wanted.backend.domain.community.application.command.UpdateReviewCommand;
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
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
            description = "자기가 시청한 강의 리뷰 1개 등록 가능"
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
            description = "리뷰 목록 조회 작성자 리뷰 최신 정렬, 비회원 리뷰 조회 및 조회 시 별점 최신화 계산/별점 분포 조회"
    )
    public ResponseEntity<ApiResponse<ReviewListResponse>> getReviews(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "latest") ReviewSortType sort,
            @RequestParam(defaultValue = "0") int page) {

        //비회원도 조회가 가능하도록
        Long currentMemberId = userDetails != null ? userDetails.getMemberId() : -1L;

        ReviewListResponse response = reviewQueryUseCase.handle(
                courseId, currentMemberId, sort, page);

        return ApiResponse.success("리뷰 목록 조회 성공", response);
    }


    @PatchMapping("/{reviewId}")
    @Operation(
            summary = "리뷰 수정",
            description = "본인이 작성한 리뷰인지 검증 후 리뷰를 수정하는 것 "
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
            description = "본인이 작성한 리뷰인지 검증 후 리뷰를 수정하는 것"
    )
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reviewId) {

        reviewCommandUseCase.delete(new DeleteReviewCommand(
                userDetails.getMemberId(), reviewId));

        return ApiResponse.successNoContent("리뷰가 삭제되었습니다.");
    }
}