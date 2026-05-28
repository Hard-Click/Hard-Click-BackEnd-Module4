package com.wanted.backend.domain.enrollment_management.presentation.api;

import com.wanted.backend.domain.enrollment_management.application.usecase.GetMyCompletedCoursesUseCase;
import com.wanted.backend.domain.enrollment_management.presentation.api.response.MyCompletedCourseResponse;
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
@RequestMapping("/api/members/me/courses/completed")
@Tag(name = "My Completed Courses", description = "마이페이지 완료 강의 목록 API")
public class MyCompletedCourseController {

    private final GetMyCompletedCoursesUseCase getMyCompletedCoursesUseCase;

    @GetMapping
    @Operation(
            summary = "완료 강의 목록 조회",
            description = "로그인한 사용자가 수강 완료한 강의 목록을 조회합니다."
    )
    public ResponseEntity<ApiResponse<List<MyCompletedCourseResponse>>> getMyCompletedCourses(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<GetMyCompletedCoursesUseCase.MyCompletedCourseView> views =
                getMyCompletedCoursesUseCase.handle(userDetails.getMemberId());

        return ApiResponse.success("수강 완료 강의 목록이 조회되었습니다.", MyCompletedCourseResponse.from(views));
    }
}
