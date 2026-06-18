package com.wanted.backend.domain.study_timer.presentation.api;

import com.wanted.backend.domain.study_timer.application.command.EndStudyTimerSessionCommand;
import com.wanted.backend.domain.study_timer.application.command.SaveStudyTimerHeartbeatCommand;
import com.wanted.backend.domain.study_timer.application.command.StartStudyTimerSessionCommand;
import com.wanted.backend.domain.study_timer.application.query.GetCurrentStudyTimerSessionQuery;
import com.wanted.backend.domain.study_timer.application.usecase.EndStudyTimerSessionUseCase;
import com.wanted.backend.domain.study_timer.application.usecase.GetCurrentStudyTimerSessionUseCase;
import com.wanted.backend.domain.study_timer.application.usecase.SaveStudyTimerHeartbeatUseCase;
import com.wanted.backend.domain.study_timer.application.usecase.StartStudyTimerSessionUseCase;
import com.wanted.backend.domain.study_timer.presentation.api.request.EndStudyTimerSessionRequest;
import com.wanted.backend.domain.study_timer.presentation.api.request.SaveStudyTimerHeartbeatRequest;
import com.wanted.backend.domain.study_timer.presentation.api.request.StartStudyTimerSessionRequest;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study-timers/sessions")
@Tag(name = "Study Timer", description = "순공시간 세션 API")
public class StudyTimerSessionController {

    private final StartStudyTimerSessionUseCase startStudyTimerSessionUseCase;
    private final SaveStudyTimerHeartbeatUseCase saveStudyTimerHeartbeatUseCase;
    private final EndStudyTimerSessionUseCase endStudyTimerSessionUseCase;
    private final GetCurrentStudyTimerSessionUseCase getCurrentStudyTimerSessionUseCase;

    @GetMapping("/current")
    @Operation(
            summary = "실행 중 순공시간 세션 조회",
            description = "현재 로그인 사용자의 실행 중인 순공시간 세션을 조회합니다. 실행 중인 세션이 없으면 data는 null입니다."
    )
    public ResponseEntity<ApiResponse<GetCurrentStudyTimerSessionUseCase.CurrentStudyTimerSessionView>> current(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        GetCurrentStudyTimerSessionUseCase.CurrentStudyTimerSessionView result =
                getCurrentStudyTimerSessionUseCase.handle(new GetCurrentStudyTimerSessionQuery(
                        userDetails.getMemberId()
                ));

        return ApiResponse.success("실행 중인 순공시간 세션을 조회했습니다.", result);
    }

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

    @PatchMapping("/{sessionId}/heartbeat")
    @Operation(
            summary = "순공시간 하트비트 저장",
            description = "실행 중인 순공시간 세션의 중간 경과 시간을 저장합니다."
    )
    public ResponseEntity<ApiResponse<SaveStudyTimerHeartbeatUseCase.StudyTimerHeartbeatView>> heartbeat(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "하트비트를 저장할 세션 ID", example = "55")
            @Positive
            @PathVariable Long sessionId,
            @Valid @RequestBody SaveStudyTimerHeartbeatRequest request
    ) {
        SaveStudyTimerHeartbeatUseCase.StudyTimerHeartbeatView result =
                saveStudyTimerHeartbeatUseCase.handle(new SaveStudyTimerHeartbeatCommand(
                        userDetails.getMemberId(),
                        sessionId,
                        request.heartbeatAt()
                ));

        return ApiResponse.success("순공시간 하트비트를 저장했습니다.", result);
    }

    @PatchMapping("/{sessionId}/end")
    @Operation(
            summary = "순공시간 세션 종료",
            description = "실행 중인 순공시간 세션을 종료하고 최종 누적 순공시간을 저장합니다."
    )
    public ResponseEntity<ApiResponse<EndStudyTimerSessionUseCase.StudyTimerSessionEndView>> end(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "종료할 세션 ID", example = "55")
            @Positive
            @PathVariable Long sessionId,
            @Valid @RequestBody EndStudyTimerSessionRequest request
    ) {
        EndStudyTimerSessionUseCase.StudyTimerSessionEndView result =
                endStudyTimerSessionUseCase.handle(new EndStudyTimerSessionCommand(
                        userDetails.getMemberId(),
                        sessionId,
                        request.endedAt()
                ));

        return ApiResponse.success("순공시간 측정을 종료했습니다.", result);
    }
}
