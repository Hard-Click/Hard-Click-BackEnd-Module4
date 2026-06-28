package com.wanted.backend.domain.grass.presentation.api;

import com.wanted.backend.domain.grass.application.query.GetDailyGrassDetailQuery;
import com.wanted.backend.domain.grass.application.query.GetGrassViewQuery;
import com.wanted.backend.domain.grass.application.query.GetLessonGrassQuery;
import com.wanted.backend.domain.grass.application.query.GetMonthlyGrassQuery;
import com.wanted.backend.domain.grass.application.query.GetStudyStreakQuery;
import com.wanted.backend.domain.grass.application.query.GetStudyTimeGrassQuery;
import com.wanted.backend.domain.grass.application.query.GetYearlyGrassQuery;
import com.wanted.backend.domain.grass.application.usecase.GetDailyGrassDetailUseCase;
import com.wanted.backend.domain.grass.application.usecase.GetGrassViewUseCase;
import com.wanted.backend.domain.grass.application.usecase.GetLessonGrassUseCase;
import com.wanted.backend.domain.grass.application.usecase.GetMonthlyGrassUseCase;
import com.wanted.backend.domain.grass.application.usecase.GetStudyStreakUseCase;
import com.wanted.backend.domain.grass.application.usecase.GetStudyTimeGrassUseCase;
import com.wanted.backend.domain.grass.application.usecase.GetYearlyGrassUseCase;
import com.wanted.backend.domain.grass.presentation.api.response.DailyGrassDetailResponse;
import com.wanted.backend.domain.grass.presentation.api.response.GrassViewResponse;
import com.wanted.backend.domain.grass.presentation.api.response.LessonGrassResponse;
import com.wanted.backend.domain.grass.presentation.api.response.MonthlyGrassResponse;
import com.wanted.backend.domain.grass.presentation.api.response.StudyStreakResponse;
import com.wanted.backend.domain.grass.presentation.api.response.StudyTimeGrassResponse;
import com.wanted.backend.domain.grass.presentation.api.response.YearlyGrassResponse;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/grass")
@Tag(name = "Grass", description = "학습 잔디 API")
public class GrassController {

    private final GetLessonGrassUseCase getLessonGrassUseCase;
    private final GetStudyTimeGrassUseCase getStudyTimeGrassUseCase;
    private final GetMonthlyGrassUseCase getMonthlyGrassUseCase;
    private final GetYearlyGrassUseCase getYearlyGrassUseCase;
    private final GetDailyGrassDetailUseCase getDailyGrassDetailUseCase;
    private final GetStudyStreakUseCase getStudyStreakUseCase;
    private final GetGrassViewUseCase getGrassViewUseCase;

    @GetMapping
    @Operation(
            summary = "잔디 보기 모드 전환 조회",
            description = "월별 또는 연간 보기 모드에 맞는 잔디 데이터를 조회합니다."
    )
    public ResponseEntity<ApiResponse<GrassViewResponse>> grass(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String view,
            @RequestParam Integer year,
            @RequestParam(required = false) Integer month
    ) {
        Long memberId = userDetails != null ? userDetails.getMemberId() : null;
        if (memberId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        GetGrassViewUseCase.GrassView result =
                getGrassViewUseCase.handle(new GetGrassViewQuery(
                        memberId,
                        view,
                        year,
                        month
                ));

        return ApiResponse.success("잔디 데이터를 조회했습니다.", GrassViewResponse.from(result));
    }

    @GetMapping("/lessons")
    @Operation(
            summary = "수강량 잔디 조회",
            description = "현재 로그인 사용자의 날짜별 수강량 기준 잔디 데이터를 조회합니다."
    )
    public ResponseEntity<ApiResponse<List<LessonGrassResponse>>> lessons(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = userDetails != null ? userDetails.getMemberId() : null;
        if (memberId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        List<GetLessonGrassUseCase.LessonGrassView> result =
                getLessonGrassUseCase.handle(new GetLessonGrassQuery(
                        memberId
                ));

        return ApiResponse.success(
                "수강량 잔디 데이터를 조회했습니다.",
                result.stream().map(LessonGrassResponse::from).toList()
        );
    }

    @GetMapping("/study-time")
    @Operation(
            summary = "순공시간 잔디 조회",
            description = "현재 로그인 사용자의 날짜별 순공시간 기준 잔디 데이터를 조회합니다."
    )
    public ResponseEntity<ApiResponse<List<StudyTimeGrassResponse>>> studyTime(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = userDetails != null ? userDetails.getMemberId() : null;
        if (memberId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        List<GetStudyTimeGrassUseCase.StudyTimeGrassView> result =
                getStudyTimeGrassUseCase.handle(new GetStudyTimeGrassQuery(
                        memberId
                ));

        return ApiResponse.success(
                "순공시간 잔디 데이터를 조회했습니다.",
                result.stream().map(StudyTimeGrassResponse::from).toList()
        );
    }

    @GetMapping("/monthly")
    @Operation(
            summary = "월별 잔디 조회",
            description = "현재 로그인 사용자의 특정 월 날짜별 학습 값 기준 월별 잔디 데이터를 조회합니다."
    )
    public ResponseEntity<ApiResponse<MonthlyGrassResponse>> monthly(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Integer year,
            @RequestParam Integer month
    ) {
        Long memberId = userDetails != null ? userDetails.getMemberId() : null;
        if (memberId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        GetMonthlyGrassUseCase.MonthlyGrassView result =
                getMonthlyGrassUseCase.handle(new GetMonthlyGrassQuery(
                        memberId,
                        year,
                        month
                ));

        return ApiResponse.success("월별 잔디 데이터를 조회했습니다.", MonthlyGrassResponse.from(result));
    }

    @GetMapping("/days/{date}")
    @Operation(
            summary = "특정 날짜 잔디 상세 조회",
            description = "현재 로그인 사용자의 특정 날짜 수강량, 순공시간, 학습 여부를 조회합니다."
    )
    public ResponseEntity<ApiResponse<DailyGrassDetailResponse>> dailyDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        Long memberId = userDetails != null ? userDetails.getMemberId() : null;
        if (memberId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        GetDailyGrassDetailUseCase.DailyGrassDetailView result =
                getDailyGrassDetailUseCase.handle(new GetDailyGrassDetailQuery(
                        memberId,
                        date
                ));

        return ApiResponse.success("잔디 상세 정보를 조회했습니다.", DailyGrassDetailResponse.from(result));
    }

    @GetMapping("/streak")
    @Operation(
            summary = "연속 학습일 조회",
            description = "현재 로그인 사용자의 오늘 기준 연속 학습일을 조회합니다."
    )
    public ResponseEntity<ApiResponse<StudyStreakResponse>> streak(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = userDetails != null ? userDetails.getMemberId() : null;
        if (memberId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        GetStudyStreakUseCase.StudyStreakView result =
                getStudyStreakUseCase.handle(new GetStudyStreakQuery(
                        memberId
                ));

        return ApiResponse.success("연속 학습일을 조회했습니다.", StudyStreakResponse.from(result));
    }

    @GetMapping("/yearly")
    @Operation(
            summary = "연간 잔디 조회",
            description = "현재 로그인 사용자의 특정 연도 날짜별 학습 값 기준 연간 잔디 데이터를 조회합니다."
    )
    public ResponseEntity<ApiResponse<YearlyGrassResponse>> yearly(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Integer year
    ) {
        Long memberId = userDetails != null ? userDetails.getMemberId() : null;
        if (memberId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        GetYearlyGrassUseCase.YearlyGrassView result =
                getYearlyGrassUseCase.handle(new GetYearlyGrassQuery(
                        memberId,
                        year
                ));

        return ApiResponse.success("연간 잔디 데이터를 조회했습니다.", YearlyGrassResponse.from(result));
    }
}
