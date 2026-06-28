ackage com.wanted.backend.domain.study_timer.presentation.api;

import com.wanted.backend.domain.study_timer.application.query.GetDailyStudyTimeQuery;
import com.wanted.backend.domain.study_timer.application.usecase.GetDailyStudyTimeUseCase;
import com.wanted.backend.domain.study_timer.presentation.api.response.DailyStudyTimeResponse;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study-timers/stats")
@Tag(name = "Study Timer Stats", description = "순공시간 통계 API")
public class StudyTimerStatsController {

    private final GetDailyStudyTimeUseCase getDailyStudyTimeUseCase;

    @GetMapping("/daily")
    @Operation(
            summary = "일별 순공시간 조회",
            description = "현재 로그인 사용자의 기간별 일별 순공시간 합계를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "일별 순공시간 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 날짜 범위"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    public ResponseEntity<ApiResponse<List<DailyStudyTimeResponse>>> daily(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "조회 시작 날짜(yyyy-MM-dd)", example = "2026-05-01")
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @Parameter(description = "조회 종료 날짜(yyyy-MM-dd)", example = "2026-05-07")
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {
        List<GetDailyStudyTimeUseCase.DailyStudyTimeItem> result =
                getDailyStudyTimeUseCase.handle(new GetDailyStudyTimeQuery(
                        userDetails.getMemberId(),
                        startDate,
                        endDate
                ));

        return ApiResponse.success(
                "일별 순공시간을 조회했습니다.",
                result.stream().map(DailyStudyTimeResponse::from).collect(Collectors.toList())
        );
    }
}
