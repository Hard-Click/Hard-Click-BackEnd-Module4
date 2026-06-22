package com.wanted.backend.domain.community.presentation.request;

import com.wanted.backend.domain.community.domain.model.ReportType;
import com.wanted.backend.domain.community.domain.model.TargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateReportRequest(

        @Schema(description = "신고 대상 유형 (POST / COMMENT / REVIEW)", example = "POST")
        @NotNull(message = "신고 대상 유형은 필수입니다.")
        TargetType targetType,

        @Schema(description = "신고 대상 ID", example = "1")
        @NotNull(message = "신고 대상 ID는 필수입니다.")
        Long targetId,

        @Schema(description = "신고 사유 목록", example = "[\"SPAM\", \"ABUSIVE_LANGUAGE\"]")
        @NotEmpty(message = "신고 사유는 최소 1개 이상이어야 합니다.")
        List<ReportType> reportTypes,

        @Schema(description = "기타 사유 (선택)", example = "불쾌한 내용입니다.")
        String reason
) {}