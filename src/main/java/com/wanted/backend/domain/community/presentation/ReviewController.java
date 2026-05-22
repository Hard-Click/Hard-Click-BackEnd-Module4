package com.wanted.backend.domain.community.presentation;

import com.wanted.backend.domain.community.application.command.CreateReviewCommand;
import com.wanted.backend.domain.community.application.usecase.ReviewCommandUseCase;
import com.wanted.backend.domain.community.application.usecase.ReviewQueryUseCase;
import com.wanted.backend.domain.community.domain.model.ReviewSortType;
import com.wanted.backend.domain.community.presentation.request.CreateReviewRequest;
import com.wanted.backend.domain.community.presentation.response.CreateReviewResponse;
import com.wanted.backend.domain.community.presentation.response.ReviewListResponse;
import com.wanted.backend.global.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<CreateReviewResponse>> createReview(
            @PathVariable Long courseId,
            @RequestHeader(value = "X-Member-Id", required = false, defaultValue = "1") Long memberId,
            @Valid @RequestBody CreateReviewRequest request) {

        Long reviewId = reviewCommandUseCase.handle(new CreateReviewCommand(
                memberId,
                courseId,
                request.rating(),
                request.content()
        ));

        return ApiResponse.created("리뷰가 등록되었습니다.", new CreateReviewResponse(reviewId));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ReviewListResponse>> getReviews(
            @PathVariable Long courseId,
            @RequestHeader(value = "X-Member-Id", required = false) Long memberId,
            @RequestParam(defaultValue = "latest") ReviewSortType sort,
            @RequestParam(defaultValue = "1") int page) {

        Long currentMemberId = memberId != null ? memberId : -1L;

        ReviewListResponse response = reviewQueryUseCase.handle(
                courseId, currentMemberId, sort, page);

        return ApiResponse.success("리뷰 목록 조회 성공", response);
    }
}