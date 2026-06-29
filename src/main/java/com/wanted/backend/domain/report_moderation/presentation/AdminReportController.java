package com.wanted.backend.domain.report_moderation.presentation;

import com.wanted.backend.domain.report_moderation.application.dto.AdminReportDecisionResult;
import com.wanted.backend.domain.report_moderation.application.dto.AdminReportDetailResult;
import com.wanted.backend.domain.report_moderation.application.dto.AdminReportListResult;
import com.wanted.backend.domain.report_moderation.application.usecase.DecideAdminReportUseCase;
import com.wanted.backend.domain.report_moderation.application.dto.AdminReportDetailResult;
import com.wanted.backend.domain.report_moderation.application.dto.AdminReportListResult;
import com.wanted.backend.domain.report_moderation.application.usecase.GetAdminReportDetailUseCase;
import com.wanted.backend.domain.report_moderation.application.usecase.GetAdminReportListUseCase;
import com.wanted.backend.domain.report_moderation.domain.model.AdminReportDecision;
import com.wanted.backend.domain.report_moderation.presentation.request.AdminReportDecisionRequest;
import com.wanted.backend.domain.report_moderation.presentation.request.AdminReportListRequest;
import com.wanted.backend.domain.report_moderation.presentation.response.AdminReportDecisionResponse;
import com.wanted.backend.domain.report_moderation.presentation.response.AdminReportDetailResponse;
import com.wanted.backend.domain.report_moderation.presentation.response.AdminReportListResponse;
import com.wanted.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@Tag(name = "Admin Report", description = "관리자 신고 관리 API")
public class AdminReportController {

    private final GetAdminReportListUseCase getAdminReportListUseCase;
    private final GetAdminReportDetailUseCase getAdminReportDetailUseCase;
    private final DecideAdminReportUseCase decideAdminReportUseCase;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "신고 목록 조회",
            description = "관리자가 신고 대상 콘텐츠 기준으로 누적 신고 3회 이상인 신고 목록을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "신고 목록 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ADMIN 권한 없음")
    })
    public ResponseEntity<ApiResponse<AdminReportListResponse>> getReports(
            @Valid @ModelAttribute AdminReportListRequest request
    ) {
        AdminReportListResult result = getAdminReportListUseCase.getList(request.toQuery());

        return ApiResponse.success(
                "신고 목록 조회 성공",
                AdminReportListResponse.from(result)
        );
    }

    @GetMapping("/{reportId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "신고 상세 조회",
            description = "동일한 신고 대상에 접수된 신고를 묶어 대상 내용, 사유별 횟수, 신고자, 대상 작성자, 누적 신고 횟수와 처리 메모를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "신고 상세 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ADMIN 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "신고를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<AdminReportDetailResponse>> getReportDetail(
            @Parameter(description = "신고 ID", example = "110")
            @PathVariable
            @Positive Long reportId
    ) {
        AdminReportDetailResult result = getAdminReportDetailUseCase.getDetail(reportId);

        return ApiResponse.success(
                "신고 상세 조회 성공",
                AdminReportDetailResponse.from(result)
        );
    }
    @PatchMapping("/{reportId}/decision")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "신고 처리 결정",
            description = "관리자가 신고를 반려하거나 신고 대상 콘텐츠를 관리자 삭제 처리합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "신고 처리 결정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 처리 결정값"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ADMIN 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "신고를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 처리된 신고")
    })
    public ResponseEntity<ApiResponse<AdminReportDecisionResponse>> decideReport(
            @Parameter(description = "신고 ID", example = "101")
            @PathVariable
            @Positive Long reportId,

            @Valid
            @RequestBody AdminReportDecisionRequest request
    ) {
        AdminReportDecisionResult result = decideAdminReportUseCase.decide(
                request.toCommand(reportId)
        );

        return ApiResponse.success(
                successMessage(result.decision()),
                AdminReportDecisionResponse.from(result)
        );
    }

    private String successMessage(AdminReportDecision decision) {
        if (decision == AdminReportDecision.REJECT) {
            return "신고가 반려되었습니다.";
        }
        return "신고 처리가 완료되었습니다.";
    }
}
