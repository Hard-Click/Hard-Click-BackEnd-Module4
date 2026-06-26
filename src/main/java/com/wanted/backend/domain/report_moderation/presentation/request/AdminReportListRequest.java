package com.wanted.backend.domain.report_moderation.presentation.request;

import com.wanted.backend.domain.community.domain.model.ReportStatus;
import com.wanted.backend.domain.community.domain.model.TargetType;
import com.wanted.backend.domain.report_moderation.application.query.AdminReportListQuery;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class AdminReportListRequest {

    @Schema(description = "처리 상태 (PENDING, RESOLVED, REJECTED 등)", example = "PENDING")
    private String status;

    @Schema(description = "신고 대상 타입 (POST, COMMENT, REVIEW)", example = "POST")
    private String targetType;

    @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
    private int page = 0;

    @Schema(description = "조회 크기", example = "10")
    @Min(value = 1, message = "조회 크기는 1 이상이어야 합니다.")
    @Max(value = 100, message = "조회 크기는 100 이하여야 합니다.")
    private int size = 10;

    public String getStatus() {
        return status;
    }

    public String getTargetType() {
        return targetType;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public AdminReportListQuery toQuery() {
        return new AdminReportListQuery(
                parseStatus(status),
                parseTargetType(targetType),
                page,
                size
        );
    }

    private ReportStatus parseStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return ReportStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, e);
        }
    }

    private TargetType parseTargetType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return TargetType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, e);
        }
    }
}
