package com.wanted.backend.domain.learning_activity.presentation.api;

import com.wanted.backend.domain.learning_activity.application.command.MemberVideoCommand;
import com.wanted.backend.domain.learning_activity.application.usecase.VideoPlayUseCase;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/learning/videos")
@Tag(name = "Learning Activity", description = "영상 재생, 학습 진도, 학습 활동 기록 API")
public class VideoPlaybackController {

    private final VideoPlayUseCase videoPlayUseCase;

    @GetMapping("/{videoId}/play")
    @Operation(
            summary = "영상 재생 정보 조회",
            description = "영상 ID를 기준으로 사용자의 수강권 또는 구독권을 검증하고 재생 가능한 스트리밍 URL과 마지막 재생 위치를 반환합니다."
    )
    public ResponseEntity<ApiResponse<VideoPlayUseCase.VideoPlayView>> play(
            @Parameter(description = "재생 정보를 조회할 영상 ID", example = "1")
            @Positive
            @PathVariable Long videoId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.success(
                "영상 재생 정보가 조회되었습니다.",
                videoPlayUseCase.handle(new MemberVideoCommand(userDetails.getMemberId(), videoId))
        );
    }
}
