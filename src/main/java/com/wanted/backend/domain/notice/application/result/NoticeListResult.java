package com.wanted.backend.domain.notice.application.result;

import java.util.List;

public record NoticeListResult(
        List<NoticeItemResult> content,
        int totalPages
) {}