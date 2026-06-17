package com.wanted.backend.domain.study_timer.presentation.api;

import com.wanted.backend.domain.study_timer.application.command.StartStudyTimerSessionCommand;
import com.wanted.backend.domain.study_timer.application.usecase.StartStudyTimerSessionUseCase;
import com.wanted.backend.domain.study_timer.presentation.api.request.StartStudyTimerSessionRequest;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study-timers/sessions")
@Tag(name = "Study Timer", description = "순공시간 세션 API")
public class StudyTimerSessionController {

    private final StartStudyTimerSessionUseCase startStudyTimerSessionUseCase;

    @PostMapping
    @Operation(
            summary = "순공시간 세션 시작",
            description = "순공시간 측정을 시작하고 실행 중인 세션 정보를 반환합니다."
    )
    public ResponseEntity<ApiResponse<StartStudyTimerSessionUseCase.StudyTimerSessionStartView>> start(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody StartStudyTimerSessionRequest request
    ) {
        StartStudyTimerSessionUseCase.StudyTimerSessionStartView result =
                startStudyTimerSessionUseCase.handle(new StartStudyTimerSessionCommand(
                        userDetails.getMemberId(),
                        request.startedAt()
                ));

        return ApiResponse.success("순공시간 측정을 시작했습니다.", result);
    }
}
