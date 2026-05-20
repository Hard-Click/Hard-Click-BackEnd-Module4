package com.wanted.backend.domain.community.presentation;

import com.wanted.backend.domain.community.application.commend.CreateReviewCommend;
import com.wanted.backend.domain.community.application.usecase.ReviewCommendUseCase;
import com.wanted.backend.domain.community.presentation.request.CreateReviewRequest;
import com.wanted.backend.domain.community.presentation.response.CreateReviewResponse;
import com.wanted.backend.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewCommendUseCase reviewCommendUseCase;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateReviewResponse>> createReview(
            @RequestHeader(value = "X-Member-Id", required = false, defaultValue = "1") Long memberId,
            @Valid @RequestBody CreateReviewRequest request) {

        Long reviewId = reviewCommendUseCase.handle(new CreateReviewCommend(
                memberId,
                request.courseId(),
                request.rating(),
                request.content()
        ));

        return ApiResponse.created(
                "리뷰가 등록되었습니다",
                new CreateReviewResponse(reviewId));
    }
}
