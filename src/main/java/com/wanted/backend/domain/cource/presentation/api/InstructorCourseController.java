package com.wanted.backend.domain.cource.presentation.api;

import com.wanted.backend.domain.cource.application.dto.CourseListResult;
import com.wanted.backend.domain.cource.application.usecase.CourseQueryUseCase;
import com.wanted.backend.domain.cource.presentation.api.response.CourseListResponse;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/instructor/courses")
@RequiredArgsConstructor
public class InstructorCourseController {

    private final CourseQueryUseCase courseQueryUseCase;

    /**
     * 강사 내 강의 목록 조회
     * GET /api/instructor/courses?page=1&size=20
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CourseListResponse>> getMyCourses(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        CourseListResult result = courseQueryUseCase.getInstructorCourses(userDetails.getMemberId(), page, size);
        return ApiResponse.success("내 강의 목록 조회 성공", CourseListResponse.from(result));
    }
}
