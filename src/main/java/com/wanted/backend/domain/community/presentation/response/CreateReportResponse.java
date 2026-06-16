package com.wanted.backend.domain.community.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateReportResponse(

        @Schema(description = "접수된 신고 내역 ID", example = "1024")
        Long reportId
) {}