package com.wanted.backend.domain.study_timer.presentation.api;

import com.wanted.backend.domain.study_timer.application.query.GetDailyStudyTimeQuery;
import com.wanted.backend.domain.study_timer.application.usecase.GetDailyStudyTimeUseCase;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

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
    public ResponseEntity<ApiResponse<List<GetDailyStudyTimeUseCase.DailyStudyTimeItem>>> daily(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "조회 시작 날짜(yyyy-MM-dd)", example = "2026-05-01")
            @RequestParam String startDate,
            @Parameter(description = "조회 종료 날짜(yyyy-MM-dd)", example = "2026-05-07")
            @RequestParam String endDate
    ) {
        List<GetDailyStudyTimeUseCase.DailyStudyTimeItem> result =
                getDailyStudyTimeUseCase.handle(new GetDailyStudyTimeQuery(
                        userDetails.getMemberId(),
                        parseDate(startDate, "시작 날짜"),
                        parseDate(endDate, "종료 날짜")
                ));

        return ApiResponse.success("일별 순공시간을 조회했습니다.", result);
    }

    private LocalDate parseDate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "는 필수입니다.");
        }

        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(fieldName + "는 yyyy-MM-dd 형식이어야 합니다.", exception);
        }
    }
}
