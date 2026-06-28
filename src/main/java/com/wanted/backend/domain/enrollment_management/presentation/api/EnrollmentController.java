package com.wanted.backend.domain.enrollment_management.presentation.api;

import com.wanted.backend.domain.enrollment_management.application.usecase.EnrollUseCase;
import com.wanted.backend.domain.enrollment_management.application.usecase.GetMyEnrollmentsUseCase;
import com.wanted.backend.domain.enrollment_management.presentation.api.request.EnrollRequest;
import com.wanted.backend.domain.enrollment_management.presentation.api.response.MyEnrollmentResponse;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    @Operation(summary = "수강신청", description = "강의 ID로 수강신청을 진행합니다. 이미 수강 중인 강의이거나 결제가 완료되지 않은 경우 실패합니다. 로그인 필요.")
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
    @Operation(summary = "내 수강 목록 조회", description = "로그인한 사용자의 수강 목록을 상태별로 조회합니다. status 파라미터로 ALL / IN_PROGRESS / COMPLETED 필터링 가능. 로그인 필요.")
    public ResponseEntity<ApiResponse<List<MyEnrollmentResponse>>> getMyEnrollments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "수강 상태 필터 (ALL / IN_PROGRESS / COMPLETED)", example = "ALL")
            @RequestParam(defaultValue = "ALL") String status
    ) {
        List<MyEnrollmentResponse> response =
                MyEnrollmentResponse.from(getMyEnrollmentsUseCase.handle(userDetails.getMemberId(), status));
        return ApiResponse.success("내 수강 목록 조회 성공", response);
    }
}
