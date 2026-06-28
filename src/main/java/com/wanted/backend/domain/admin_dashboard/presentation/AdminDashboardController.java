package com.wanted.backend.domain.admin_dashboard.presentation;

import com.wanted.backend.domain.admin_dashboard.application.dto.AdminDashboardResult;
import com.wanted.backend.domain.admin_dashboard.application.usecase.GetAdminDashboardUseCase;
import com.wanted.backend.domain.admin_dashboard.presentation.response.AdminDashboardResponse;
import com.wanted.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard", description = "관리자 대시보드 API")
public class AdminDashboardController {

    private final GetAdminDashboardUseCase getAdminDashboardUseCase;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "관리자 대시보드 조회",
            description = "회원, 신고, 강의, 공지 통계와 최근 신고 및 공지를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "관리자 대시보드 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ADMIN 권한 없음")
    })
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard() {
        AdminDashboardResult result =
                getAdminDashboardUseCase.getDashboard();

        return ApiResponse.success(
                "관리자 대시보드 조회 성공",
                AdminDashboardResponse.from(result)
        );
    }
}