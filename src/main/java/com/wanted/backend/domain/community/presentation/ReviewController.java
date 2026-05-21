package com.wanted.backend.domain.community.presentation;


import com.wanted.backend.domain.community.application.command.CreateReviewCommand;
import com.wanted.backend.domain.community.application.usecase.ReviewCommandUseCase;
import com.wanted.backend.domain.community.presentation.request.CreateReviewRequest;
import com.wanted.backend.domain.community.presentation.response.CreateReviewResponse;
import com.wanted.backend.global.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses/{courseId}/reviews")
public class ReviewController {

    private final ReviewCommandUseCase reviewCommandUseCase;

    public ReviewController(ReviewCommandUseCase reviewCommandUseCase) {
        this.reviewCommandUseCase = reviewCommandUseCase;
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

        return ApiResponse.created(
                "리뷰가 등록되었습니다",
                new CreateReviewResponse(reviewId));
    }
}
