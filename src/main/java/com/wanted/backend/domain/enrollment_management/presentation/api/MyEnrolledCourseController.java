package com.wanted.backend.domain.enrollment_management.presentation.api;


import com.wanted.backend.domain.enrollment_management.application.usecase.GetMyEnrolledCourseUseCase;
import com.wanted.backend.domain.enrollment_management.presentation.api.response.MyEnrolledCourseResponse;
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
@RequestMapping("/api/members/me/courses")
@RequiredArgsConstructor
@Tag(name = "My Enrolled Course" , description = "마이페이지 수강 강의 목록 API")
public class MyEnrolledCourseController {

    private final GetMyEnrolledCourseUseCase getMyEnrolledCourseUseCase;

    @GetMapping
    @Operation(
            summary = "내 수강 강의 목록 조회",
            description = "로그인한 사용자가 수강 중인 강의 목록을 진도율과 이어보기 정보(마지막 영상 ID, 재생 위치)와 함께 조회합니다. 로그인 필요."
    )
    public ResponseEntity<ApiResponse<List<MyEnrolledCourseResponse>>> getMyEnrolledCourses(
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {
        List<GetMyEnrolledCourseUseCase.MyEnrolledCourseView> views = getMyEnrolledCourseUseCase.handle(userDetails.getMemberId());

        List<MyEnrolledCourseResponse> myEnrolledCourseResponses = MyEnrolledCourseResponse.from(views);

        return ApiResponse.success("내 수강 강의 목록을 조회했습니다.", myEnrolledCourseResponses);
    }

}
