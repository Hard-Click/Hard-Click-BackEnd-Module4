package com.wanted.backend.domain.payment.presentation.response;

import com.wanted.backend.domain.payment.application.port.AdminPaymentQueryPort;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "관리자 결제 목록 응답")
public record AdminPaymentListResponse(
        @Schema(description = "현재 페이지의 결제 내역 목록")
        List<AdminPaymentResponse> content,

        @Schema(description = "현재 페이지 번호", example = "0")
        int page,

        @Schema(description = "페이지 크기", example = "20")
        int size,

        @Schema(description = "전체 결제 내역 수", example = "4")
        long totalElements,

        @Schema(description = "전체 페이지 수", example = "1")
        int totalPages,

        @Schema(description = "마지막 페이지 여부", example = "true")
        boolean last
) {
    public static AdminPaymentListResponse from(Page<AdminPaymentQueryPort.AdminPaymentData> page) {
        List<AdminPaymentResponse> content = page.getContent().stream()
                .map(AdminPaymentResponse::from)
                .toList();
        return new AdminPaymentListResponse(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
