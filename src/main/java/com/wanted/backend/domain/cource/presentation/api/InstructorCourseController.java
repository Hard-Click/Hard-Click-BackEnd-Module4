package com.wanted.backend.domain.cource.presentation.api;

import com.wanted.backend.domain.cource.application.dto.CourseListResult;
import com.wanted.backend.domain.cource.application.usecase.CourseQueryUseCase;
import com.wanted.backend.domain.cource.presentation.api.response.CourseListResponse;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/instructor/courses")
@RequiredArgsConstructor
@Tag(name = "Instructor Courses", description = "강사 본인 강의 관리 API (/api/instructor/courses)")
public class InstructorCourseController {

    private final CourseQueryUseCase courseQueryUseCase;

    @GetMapping
    @Operation(summary = "강사 내 강의 목록 조회", description = "로그인한 강사 본인이 등록한 강의 목록을 페이징 조회합니다. INSTRUCTOR 권한 필요.")
    public ResponseEntity<ApiResponse<CourseListResponse>> getMyCourses(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "페이지 번호 (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지당 항목 수", example = "20") @RequestParam(defaultValue = "20") int size
    ) {
        CourseListResult result = courseQueryUseCase.getInstructorCourses(userDetails.getMemberId(), page, size);
        return ApiResponse.success("내 강의 목록 조회 성공", CourseListResponse.from(result));
    }
}
