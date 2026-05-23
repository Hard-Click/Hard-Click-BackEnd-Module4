package com.wanted.backend.domain.learning_activity.presentation.api;

import com.wanted.backend.domain.learning_activity.application.command.CompleteVideoCommand;
import com.wanted.backend.domain.learning_activity.application.command.GetVideoPositionCommand;
import com.wanted.backend.domain.learning_activity.application.command.SaveVideoPositionCommand;
import com.wanted.backend.domain.learning_activity.application.command.SaveWatchTimeCommand;
import com.wanted.backend.domain.learning_activity.application.usecase.CompleteVideoUseCase;
import com.wanted.backend.domain.learning_activity.application.usecase.GetVideoPositionUseCase;
import com.wanted.backend.domain.learning_activity.application.usecase.SaveVideoPositionUseCase;
import com.wanted.backend.domain.learning_activity.application.usecase.SaveWatchTimeUseCase;
import com.wanted.backend.domain.learning_activity.presentation.api.request.SaveVideoPositionRequest;
import com.wanted.backend.domain.learning_activity.presentation.api.request.SaveWatchTimeRequest;
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
    private final SaveWatchTimeUseCase saveWatchTimeUseCase;
    private final CompleteVideoUseCase completeVideoUseCase;

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

    @PatchMapping("/{videoId}/progress/watch-time")
    @Operation(
            summary = "영상 시청 시간 누적 저장",
            description = "영상 시청 중 추가로 시청한 시간을 초 단위로 누적 저장합니다."
    )
    public ResponseEntity<ApiResponse<Void>> saveWatchTime(
            @Parameter(description = "시청 시간을 누적 저장할 영상 ID", example = "10")
            @Positive
            @PathVariable Long videoId,
            @Valid @RequestBody SaveWatchTimeRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        saveWatchTimeUseCase.handle(new SaveWatchTimeCommand(
                userDetails.getMemberId(),
                videoId,
                request.watchTimeSeconds()
        ));
        return ApiResponse.successNoContent("영상 시청 시간이 누적 저장되었습니다.");
    }

    @PatchMapping("/{videoId}/progress/complete")
    @Operation(
            summary = "영상 완료 처리",
            description = "영상 시청 비율이 90% 이상이면 완료 상태로 반영합니다."
    )
    public ResponseEntity<ApiResponse<Void>> complete(
            @Parameter(description = "완료 처리할 영상 ID", example = "10")
            @Positive
            @PathVariable Long videoId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        completeVideoUseCase.handle(new CompleteVideoCommand(userDetails.getMemberId(), videoId));
        return ApiResponse.successNoContent("영상 완료 처리되었습니다.");
    }
}
