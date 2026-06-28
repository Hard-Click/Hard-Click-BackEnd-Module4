package com.wanted.backend.domain.community.presentation;

import com.wanted.backend.domain.community.application.usecase.GetMyActivityUseCase;
import com.wanted.backend.domain.community.presentation.response.MyActivityResponse;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members/me/activities")
@Tag(name = "My Activity", description = "마이페이지 활동 조회 API")
public class MyActivityController {

    private final GetMyActivityUseCase getMyActivityUseCase;

    @GetMapping
    @Operation(
            summary = "내 활동 조회",
            description = "로그인한 사용자가 작성한 리뷰, 게시글, 댓글 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 활동 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<ApiResponse<MyActivityResponse>> getMyActivities(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.success(
                "내 활동이 조회되었습니다.",
                MyActivityResponse.from(getMyActivityUseCase.handle(userDetails.getMemberId()))
        );
    }
}
