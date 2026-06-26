package com.wanted.backend.domain.notice.application.usecase;

import com.wanted.backend.domain.notice.application.command.GetNoticeListCommand;
import com.wanted.backend.domain.notice.presentation.response.NoticeDetailResponse;
import com.wanted.backend.domain.notice.presentation.response.NoticeListResponse;

public interface NoticeQueryUseCase {
    NoticeListResponse getList(GetNoticeListCommand command);
    // 읽음 여부 반영을 위해 memberId 추가
    NoticeDetailResponse getDetail(Long noticeId, Long memberId);
}