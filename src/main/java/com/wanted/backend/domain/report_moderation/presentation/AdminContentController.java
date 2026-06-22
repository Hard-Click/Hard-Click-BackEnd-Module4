package com.wanted.backend.domain.report_moderation.presentation;

import com.wanted.backend.domain.community.domain.model.TargetType;
import com.wanted.backend.domain.report_moderation.application.dto.AdminContentResult;
import com.wanted.backend.domain.report_moderation.application.dto.AdminContentStatusResult;
import com.wanted.backend.domain.report_moderation.application.usecase.ChangeAdminContentStatusUseCase;
import com.wanted.backend.domain.report_moderation.application.usecase.GetAdminContentUseCase;
import com.wanted.backend.domain.report_moderation.presentation.request.AdminContentStatusRequest;
import com.wanted.backend.domain.report_moderation.presentation.response.AdminContentResponse;
import com.wanted.backend.domain.report_moderation.presentation.response.AdminContentStatusResponse;
import com.wanted.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/admin/contents")
@RequiredArgsConstructor
@Tag(name = "Admin Content", description = "관리자 콘텐츠 관리 API")
public class AdminContentController {

    private final GetAdminContentUseCase getAdminContentUseCase;
    private final ChangeAdminContentStatusUseCase changeAdminContentStatusUseCase;

    @GetMapping("/{contentType}/{contentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "신고 대상 콘텐츠 조회",
            description = "관리자가 신고 대상이 된 게시글, 댓글, 리뷰의 내용을 조회합니다."
    )
    public ResponseEntity<ApiResponse<AdminContentResponse>> getContent(
            @Parameter(description = "콘텐츠 타입", example = "POST")
            @PathVariable TargetType contentType,
            @Parameter(description = "콘텐츠 ID", example = "15")
            @PathVariable
            @Positive Long contentId
    ) {
        AdminContentResult result = getAdminContentUseCase.getContent(contentType, contentId);

        return ApiResponse.success(
                "콘텐츠 조회 성공",
                AdminContentResponse.from(result)
        );
    }

    @PatchMapping("/{contentType}/{contentId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "콘텐츠 상태 변경",
            description = "관리자가 신고 대상이 된 게시글, 댓글, 리뷰를 관리자 삭제(ADMIN_DELETED) 처리합니다."
    )
    public ResponseEntity<ApiResponse<AdminContentStatusResponse>> changeStatus(
            @Parameter(description = "콘텐츠 타입", example = "POST")
            @PathVariable TargetType contentType,
            @Parameter(description = "콘텐츠 ID", example = "15")
            @PathVariable
            @Positive Long contentId,
            @Valid @RequestBody AdminContentStatusRequest request
    ) {
        AdminContentStatusResult result = changeAdminContentStatusUseCase.changeStatus(
                request.toCommand(contentType, contentId)
        );

        return ApiResponse.success(
                "콘텐츠 상태가 변경되었습니다.",
                AdminContentStatusResponse.from(result)
        );
    }
}
