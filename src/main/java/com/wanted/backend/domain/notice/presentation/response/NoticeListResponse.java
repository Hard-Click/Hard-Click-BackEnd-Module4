package com.wanted.backend.domain.notice.presentation.response;

import com.wanted.backend.domain.notice.application.result.NoticeListResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record NoticeListResponse(

        @Schema(description = "공지사항 목록")
        List<NoticeItemResponse> content,

        @Schema(description = "전체 페이지 수", example = "3")
        int totalPages
) {
        public static NoticeListResponse from(NoticeListResult result) {
                return new NoticeListResponse(
                        result.content().stream().map(NoticeItemResponse::from).toList(),
                        result.totalPages());
        }
}