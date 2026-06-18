package com.wanted.backend.domain.report_moderation.presentation;

import com.wanted.backend.domain.report_moderation.application.dto.AdminReportListResult;
import com.wanted.backend.domain.report_moderation.application.usecase.GetAdminReportListUseCase;
import com.wanted.backend.domain.report_moderation.presentation.request.AdminReportListRequest;
import com.wanted.backend.domain.report_moderation.presentation.response.AdminReportListResponse;
import com.wanted.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@Tag(name = "Admin Report", description = "관리자 신고 관리 API")
public class AdminReportController {

    private final GetAdminReportListUseCase getAdminReportListUseCase;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "신고 목록 조회",
            description = "관리자가 신고 대상 콘텐츠 기준으로 누적 신고 3회 이상인 신고 목록을 조회합니다."
    )
    public ResponseEntity<ApiResponse<AdminReportListResponse>> getReports(
            @Valid @ModelAttribute AdminReportListRequest request
    ) {
        AdminReportListResult result = getAdminReportListUseCase.getList(request.toQuery());

        return ApiResponse.success(
                "신고 목록 조회 성공",
                AdminReportListResponse.from(result)
        );
    }
}
