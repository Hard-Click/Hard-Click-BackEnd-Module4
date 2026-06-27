package com.wanted.backend.domain.community.presentation;

import com.wanted.backend.domain.community.application.command.CreateReportCommand;
import com.wanted.backend.domain.community.application.usecase.ReportCommandUseCase;
import com.wanted.backend.domain.community.presentation.request.CreateReportRequest;
import com.wanted.backend.domain.community.presentation.response.CreateReportResponse;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Community Report", description = "커뮤니티 신고 API")
@RestController
@RequestMapping("/api")
public class ReportController {

    private final ReportCommandUseCase reportCommandUseCase;

    public ReportController(ReportCommandUseCase reportCommandUseCase) {
        this.reportCommandUseCase = reportCommandUseCase;
    }

    @PostMapping("/reports")
    @Operation(
            summary = "신고 접수",
            description = """
                    게시글, 댓글, 리뷰를 신고합니다.
                    - 로그인한 회원만 신고할 수 있습니다.
                    - 동일한 대상에 중복 신고는 불가합니다.
                    - 신고 사유는 최소 1개 이상 선택해야 합니다.
                    """
    )
    public ResponseEntity<ApiResponse<CreateReportResponse>> createReport(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CreateReportRequest request) {

        Long reportId = reportCommandUseCase.create(new CreateReportCommand(
                userDetails.getMemberId(),
                request.targetType(),
                request.targetId(),
                request.reportTypes(),
                request.reason()
        ));

        return ApiResponse.created("신고가 접수되었습니다.", new CreateReportResponse(reportId));
    }
}