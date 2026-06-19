package com.wanted.backend.domain.grass.presentation.api;

import com.wanted.backend.domain.grass.application.query.GetLessonGrassQuery;
import com.wanted.backend.domain.grass.application.query.GetStudyTimeGrassQuery;
import com.wanted.backend.domain.grass.application.usecase.GetLessonGrassUseCase;
import com.wanted.backend.domain.grass.application.usecase.GetStudyTimeGrassUseCase;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/grass")
@Tag(name = "Grass", description = "학습 잔디 API")
public class GrassController {

    private final GetLessonGrassUseCase getLessonGrassUseCase;
    private final GetStudyTimeGrassUseCase getStudyTimeGrassUseCase;

    @GetMapping("/lessons")
    @Operation(
            summary = "수강량 잔디 조회",
            description = "현재 로그인 사용자의 날짜별 수강량 기준 잔디 데이터를 조회합니다."
    )
    public ResponseEntity<ApiResponse<List<GetLessonGrassUseCase.LessonGrassView>>> lessons(
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

        return ApiResponse.success("수강량 잔디 데이터를 조회했습니다.", result);
    }

    @GetMapping("/study-time")
    @Operation(
            summary = "순공시간 잔디 조회",
            description = "현재 로그인 사용자의 날짜별 순공시간 기준 잔디 데이터를 조회합니다."
    )
    public ResponseEntity<ApiResponse<List<GetStudyTimeGrassUseCase.StudyTimeGrassView>>> studyTime(
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

        return ApiResponse.success("순공시간 잔디 데이터를 조회했습니다.", result);
    }
}
