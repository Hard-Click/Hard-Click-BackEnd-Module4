package com.wanted.backend.domain.notice.presentation.response;

import java.time.LocalDateTime;
import java.util.List;


public record NoticeListResponse(
        List<NoticeItemResponse> content,
        int totalPages
) {

}