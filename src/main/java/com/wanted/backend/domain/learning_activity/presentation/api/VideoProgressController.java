package com.wanted.backend.domain.learning_activity.presentation.api;

import com.wanted.backend.domain.learning_activity.application.command.GetVideoPositionCommand;
import com.wanted.backend.domain.learning_activity.application.command.SaveVideoPositionCommand;
import com.wanted.backend.domain.learning_activity.application.usecase.GetVideoPositionUseCase;
import com.wanted.backend.domain.learning_activity.application.usecase.SaveVideoPositionUseCase;
import com.wanted.backend.domain.learning_activity.presentation.api.request.SaveVideoPositionRequest;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/learning/videos")
@Tag(name = "Learning Activity", description = "영상 재생, 학습 진도, 학습 활동 기록 API")
public class VideoProgressController {

    private final GetVideoPositionUseCase getVideoPositionUseCase;
    private final SaveVideoPositionUseCase saveVideoPositionUseCase;

    @GetMapping("/{videoId}/progress/position")
    @Operation(
            summary = "이어보기 위치 조회",
            description = "영상의 마지막 재생 위치를 초 단위로 조회합니다."
    )
    public ResponseEntity<ApiResponse<GetVideoPositionUseCase.VideoPositionView>> getPosition(
            @Parameter(description = "이어보기 위치를 조회할 영상 ID", example = "10")
            @Positive
            @PathVariable Long videoId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.success(
                "이어보기 위치가 조회되었습니다.",
                getVideoPositionUseCase.handle(new GetVideoPositionCommand(userDetails.getMemberId(), videoId))
        );
    }

    @PatchMapping("/{videoId}/progress/position")
    @Operation(
            summary = "마지막 재생 위치 저장",
            description = "영상 재생 중 마지막 재생 위치를 초 단위로 저장하여 이어보기를 지원합니다."
    )
    public ResponseEntity<ApiResponse<Void>> savePosition(
            @Parameter(description = "재생 위치를 저장할 영상 ID", example = "10")
            @Positive
            @PathVariable Long videoId,
            @Valid @RequestBody SaveVideoPositionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        saveVideoPositionUseCase.handle(new SaveVideoPositionCommand(
                userDetails.getMemberId(),
                videoId,
                request.positionSeconds()
        ));
        return ApiResponse.successNoContent("마지막 재생 위치가 저장되었습니다.");
    }
}
