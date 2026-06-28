package com.wanted.backend.domain.enrollment_management.presentation.api;

import com.wanted.backend.domain.enrollment_management.application.usecase.EnrollUseCase;
import com.wanted.backend.domain.enrollment_management.application.usecase.GetMyEnrollmentsUseCase;
import com.wanted.backend.domain.enrollment_management.presentation.api.request.EnrollRequest;
import com.wanted.backend.domain.enrollment_management.presentation.api.response.MyEnrollmentResponse;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Enrollment", description = "수강 등록 API")
@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollUseCase enrollUseCase;
    private final GetMyEnrollmentsUseCase getMyEnrollmentsUseCase;

    /**
     * 수강신청
     * POST /api/enrollments
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Long>>> enroll(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody EnrollRequest request
    ) {
        Long enrollmentId = enrollUseCase.handle(request.toCommand(userDetails.getMemberId()));
        return ApiResponse.created("수강신청이 완료되었습니다.", Map.of("enrollmentId", enrollmentId));
    }

    /**
     * 내 수강 목록 조회
     * GET /api/enrollments/me?status=ALL|IN_PROGRESS|COMPLETED
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<MyEnrollmentResponse>>> getMyEnrollments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "ALL") String status
    ) {
        List<MyEnrollmentResponse> response =
                MyEnrollmentResponse.from(getMyEnrollmentsUseCase.handle(userDetails.getMemberId(), status));
        return ApiResponse.success("내 수강 목록 조회 성공", response);
    }
}
