package com.wanted.backend.domain.notice.application.usecase;

import com.wanted.backend.domain.notice.application.command.GetNoticeListCommand;
import com.wanted.backend.domain.notice.presentation.response.NoticeDetailResponse;
import com.wanted.backend.domain.notice.presentation.response.NoticeListResponse;

public interface NoticeQueryUseCase {
    NoticeListResponse getList(GetNoticeListCommand command);
    NoticeDetailResponse getDetail(Long noticeId);
}