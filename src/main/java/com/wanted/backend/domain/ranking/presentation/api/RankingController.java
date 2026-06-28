package com.wanted.backend.domain.ranking.presentation.api;

import com.wanted.backend.domain.ranking.application.query.GetAcceptedCommentRankingQuery;
import com.wanted.backend.domain.ranking.application.query.GetLessonRankingQuery;
import com.wanted.backend.domain.ranking.application.query.GetMyRankingDetailQuery;
import com.wanted.backend.domain.ranking.application.query.GetMyRankingSummaryQuery;
import com.wanted.backend.domain.ranking.application.query.GetStudyTimeRankingQuery;
import com.wanted.backend.domain.ranking.application.usecase.GetAcceptedCommentRankingUseCase;
import com.wanted.backend.domain.ranking.application.usecase.GetLessonRankingUseCase;
import com.wanted.backend.domain.ranking.application.usecase.GetMyRankingDetailUseCase;
import com.wanted.backend.domain.ranking.application.usecase.GetMyRankingSummaryUseCase;
import com.wanted.backend.domain.ranking.application.usecase.GetStudyTimeRankingUseCase;
import com.wanted.backend.domain.ranking.presentation.api.response.AcceptedCommentRankingResponse;
import com.wanted.backend.domain.ranking.presentation.api.response.LessonRankingResponse;
import com.wanted.backend.domain.ranking.presentation.api.response.MyRankingDetailResponse;
import com.wanted.backend.domain.ranking.presentation.api.response.MyRankingSummaryResponse;
import com.wanted.backend.domain.ranking.presentation.api.response.StudyTimeRankingResponse;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rankings")
@Tag(name = "Ranking", description = "랭킹 API")
public class RankingController {

    private final GetMyRankingDetailUseCase getMyRankingDetailUseCase;
    private final GetMyRankingSummaryUseCase getMyRankingSummaryUseCase;
    private final GetStudyTimeRankingUseCase getStudyTimeRankingUseCase;
    private final GetLessonRankingUseCase getLessonRankingUseCase;
    private final GetAcceptedCommentRankingUseCase getAcceptedCommentRankingUseCase;

    @GetMapping("/study-time")
    @Operation(
            summary = "순공시간 랭킹 조회",
            description = "기간별 순공시간 랭킹 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "순공시간 랭킹 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    public ResponseEntity<ApiResponse<StudyTimeRankingResponse>> studyTime(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "조회 기간 (daily | weekly | monthly | alltime). 미지정 시 alltime", example = "weekly")
            @RequestParam(required = false) String period
    ) {
        Long memberId = userDetails != null ? userDetails.getMemberId() : null;
        if (memberId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        GetStudyTimeRankingUseCase.StudyTimeRankingView result =
                getStudyTimeRankingUseCase.handle(new GetStudyTimeRankingQuery(period));

        return ApiResponse.success("순공시간 랭킹을 조회했습니다.", StudyTimeRankingResponse.from(result));
    }

    @GetMapping("/lessons")
    @Operation(
            summary = "수강량 랭킹 조회",
            description = "기간별 수강량 랭킹 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수강량 랭킹 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    public ResponseEntity<ApiResponse<LessonRankingResponse>> lessons(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "조회 기간 (daily | weekly | monthly | alltime). 미지정 시 alltime", example = "weekly")
            @RequestParam(required = false) String period
    ) {
        Long memberId = userDetails != null ? userDetails.getMemberId() : null;
        if (memberId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        GetLessonRankingUseCase.LessonRankingView result =
                getLessonRankingUseCase.handle(new GetLessonRankingQuery(period));

        return ApiResponse.success("수강량 랭킹을 조회했습니다.", LessonRankingResponse.from(result));
    }

    @GetMapping("/accepted-comments")
    @Operation(
            summary = "댓글 채택 수 랭킹 조회",
            description = "기간별 댓글 채택 수 랭킹 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 채택 수 랭킹 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    public ResponseEntity<ApiResponse<AcceptedCommentRankingResponse>> acceptedComments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "조회 기간 (daily | weekly | monthly | alltime). 미지정 시 alltime", example = "weekly")
            @RequestParam(required = false) String period
    ) {
        Long memberId = userDetails != null ? userDetails.getMemberId() : null;
        if (memberId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        GetAcceptedCommentRankingUseCase.AcceptedCommentRankingView result =
                getAcceptedCommentRankingUseCase.handle(new GetAcceptedCommentRankingQuery(period));

        return ApiResponse.success("댓글 채택 수 랭킹을 조회했습니다.", AcceptedCommentRankingResponse.from(result));
    }

    @GetMapping("/me/summary")
    @Operation(
            summary = "내 랭킹 요약 정보 조회",
            description = "마이페이지 랭킹 요약 영역에서 사용할 순공시간, 수강량, 댓글 채택 수 순위와 상위 퍼센트를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 랭킹 요약 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    public ResponseEntity<ApiResponse<MyRankingSummaryResponse>> mySummary(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = userDetails != null ? userDetails.getMemberId() : null;
        if (memberId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        GetMyRankingSummaryUseCase.MyRankingSummaryView result =
                getMyRankingSummaryUseCase.handle(new GetMyRankingSummaryQuery(memberId));

        return ApiResponse.success("내 랭킹 요약 정보를 조회했습니다.", MyRankingSummaryResponse.from(result));
    }

    @GetMapping("/me")
    @Operation(
            summary = "내 랭킹 상세 정보 조회",
            description = "랭킹 탭에서 사용할 선택 기준별 내 순위, 전체 사용자 수, 상위 퍼센트를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 랭킹 상세 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    public ResponseEntity<ApiResponse<MyRankingDetailResponse>> me(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "랭킹 기준 (studyTime | lesson | acceptedComment). 미지정 시 studyTime", example = "studyTime")
            @RequestParam(required = false) String metric,
            @Parameter(description = "조회 기간 (daily | weekly | monthly | alltime). 미지정 시 alltime", example = "weekly")
            @RequestParam(required = false) String period
    ) {
        Long memberId = userDetails != null ? userDetails.getMemberId() : null;
        if (memberId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        GetMyRankingDetailUseCase.MyRankingDetailView result =
                getMyRankingDetailUseCase.handle(new GetMyRankingDetailQuery(
                        memberId,
                        metric,
                        period
                ));

        return ApiResponse.success("내 랭킹 상세 정보를 조회했습니다.", MyRankingDetailResponse.from(result));
    }
}
