package com.wanted.backend.domain.payment.presentation.response;

import com.wanted.backend.domain.payment.application.usecase.GetMyPaymentHistoryUseCase;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

public record MyPaymentHistoryPageResponse(
        @Schema(description = "현재 페이지의 결제 내역 목록")
        List<MyPaymentHistoryResponse> content,

        @Schema(description = "현재 페이지 번호", example = "0")
        int page,

        @Schema(description = "페이지 크기", example = "10")
        int size,

        @Schema(description = "전체 결제 내역 수", example = "24")
        long totalElements,

        @Schema(description = "전체 페이지 수", example = "3")
        int totalPages,

        @Schema(description = "마지막 페이지 여부", example = "false")
        boolean last
) {

    public static MyPaymentHistoryPageResponse from(Page<GetMyPaymentHistoryUseCase.MyPaymentHistoryView> page) {
        return new MyPaymentHistoryPageResponse(
                MyPaymentHistoryResponse.from(page.getContent()),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
