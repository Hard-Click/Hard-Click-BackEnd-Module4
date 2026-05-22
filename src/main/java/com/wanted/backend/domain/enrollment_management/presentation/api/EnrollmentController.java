package com.wanted.backend.domain.enrollment_management.presentation.api;

import com.wanted.backend.domain.enrollment_management.application.usecase.EnrollUseCase;
import com.wanted.backend.domain.enrollment_management.presentation.api.request.EnrollRequest;
import com.wanted.backend.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollUseCase enrollUseCase;

    /**
     * 수강신청
     * POST /api/enrollments
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Long>>> enroll(
            @RequestHeader("X-Member-Id") Long userId,
            @Valid @RequestBody EnrollRequest request
    ) {
        Long enrollmentId = enrollUseCase.handle(request.toCommand(userId));
        return ApiResponse.created("수강신청이 완료되었습니다.", Map.of("enrollmentId", enrollmentId));
    }
}
