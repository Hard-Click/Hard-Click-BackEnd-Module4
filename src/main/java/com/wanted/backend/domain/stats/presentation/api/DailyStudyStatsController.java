package com.wanted.backend.domain.stats.presentation.api;

import com.wanted.backend.domain.stats.application.query.GetDailyStudyStatQuery;
import com.wanted.backend.domain.stats.application.usecase.GetDailyStudyStatUseCase;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stats")
@Tag(name = "Stats", description = "학습 통계 API")
public class DailyStudyStatsController {

    private final GetDailyStudyStatUseCase getDailyStudyStatUseCase;

    @GetMapping("/daily-study/{date}")
    @Operation(
            summary = "특정 날짜 학습 통계 조회",
            description = "현재 로그인 사용자의 특정 날짜 학습 상세 통계를 조회합니다."
    )
    public ResponseEntity<ApiResponse<GetDailyStudyStatUseCase.DailyStudyStatView>> dailyStudy(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "조회 날짜(yyyy-MM-dd)", example = "2026-06-18")
            @PathVariable
            String date
    ) {
        GetDailyStudyStatUseCase.DailyStudyStatView result =
                getDailyStudyStatUseCase.handle(new GetDailyStudyStatQuery(
                        userDetails.getMemberId(),
                        parseDate(date)
                ));

        return ApiResponse.success("특정 날짜 학습 통계를 조회했습니다.", result);
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.DAILY_STATS_DATE_REQUIRED);
        }

        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new BusinessException(ErrorCode.DAILY_STATS_DATE_FORMAT_INVALID, exception);
        }
    }
}
