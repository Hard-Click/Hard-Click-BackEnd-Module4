package com.wanted.backend.domain.grass.presentation.api;

import com.wanted.backend.domain.grass.application.query.GetLessonGrassQuery;
import com.wanted.backend.domain.grass.application.usecase.GetLessonGrassUseCase;
import com.wanted.backend.global.common.ApiResponse;
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

    @GetMapping("/lessons")
    @Operation(
            summary = "수강량 잔디 조회",
            description = "현재 로그인 사용자의 날짜별 수강량 기준 잔디 데이터를 조회합니다."
    )
    public ResponseEntity<ApiResponse<List<GetLessonGrassUseCase.LessonGrassView>>> lessons(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<GetLessonGrassUseCase.LessonGrassView> result =
                getLessonGrassUseCase.handle(new GetLessonGrassQuery(
                        userDetails.getMemberId()
                ));

        return ApiResponse.success("수강량 잔디 데이터를 조회했습니다.", result);
    }
}
