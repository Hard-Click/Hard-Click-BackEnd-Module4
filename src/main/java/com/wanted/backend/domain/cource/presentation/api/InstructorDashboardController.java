package com.wanted.backend.domain.cource.presentation.api;

import com.wanted.backend.domain.cource.application.dto.InstructorDashboardResult;
import com.wanted.backend.domain.cource.application.usecase.CourseQueryUseCase;
import com.wanted.backend.domain.cource.presentation.api.response.InstructorDashboardResponse;
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

@RestController
@RequestMapping("/api/instructor/dashboard")
@RequiredArgsConstructor
@Tag(name = "Instructor Dashboard", description = "강사 대시보드 API")
public class InstructorDashboardController {

    private final CourseQueryUseCase courseQueryUseCase;

    @GetMapping
    @Operation(
            summary = "강사 대시보드 통계 조회",
            description = "로그인한 강사 본인의 강의/수강생/퀴즈 통계를 조회합니다. INSTRUCTOR 권한 필요. " +
                    "퀴즈 수는 quiz 도메인이 Mock API라 임시 고정값입니다."
    )
    public ResponseEntity<ApiResponse<InstructorDashboardResponse>> getDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        InstructorDashboardResult result = courseQueryUseCase.getInstructorDashboard(userDetails.getMemberId());
        return ApiResponse.success("강사 대시보드 조회 성공", InstructorDashboardResponse.from(result));
    }
}
