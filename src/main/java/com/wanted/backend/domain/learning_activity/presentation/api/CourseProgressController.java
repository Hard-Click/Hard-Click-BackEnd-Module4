package com.wanted.backend.domain.learning_activity.presentation.api;

import com.wanted.backend.domain.learning_activity.application.command.GetCourseProgressCommand;
import com.wanted.backend.domain.learning_activity.application.usecase.GetCourseProgressUseCase;
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
@RequestMapping("/api/learning/courses")
@Tag(name = "Learning Activity", description = "영상 재생, 학습 진도, 학습 활동 기록 API")
public class CourseProgressController {

    private final GetCourseProgressUseCase getCourseProgressUseCase;

    @GetMapping("/{courseId}/progress")
    @Operation(
            summary = "강의 전체 진도율 조회",
            description = "강의의 전체 진도율과 영상별 학습 상태를 조회합니다."
    )
    public ResponseEntity<ApiResponse<GetCourseProgressUseCase.CourseProgressView>> getCourseProgress(
            @Parameter(description = "진도율을 조회할 강의 ID", example = "20")
            @Positive
            @PathVariable Long courseId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.success(
                "강의 전체 진도율이 조회되었습니다.",
                getCourseProgressUseCase.handle(new GetCourseProgressCommand(userDetails.getMemberId(), courseId))
        );
    }
}
