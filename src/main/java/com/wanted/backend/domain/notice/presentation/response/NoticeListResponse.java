package com.wanted.backend.domain.notice.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;


public record NoticeListResponse(

        @Schema(description = "공지사항 목록")
        List<NoticeItemResponse> content,

        @Schema(description = "전체 페이지 수", example = "3")
        int totalPages

) {

}