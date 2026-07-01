package com.wanted.backend.domain.notice.application.usecase;

import com.wanted.backend.domain.notice.application.command.GetNoticeListCommand;
import com.wanted.backend.domain.notice.application.result.NoticeDetailResult;
import com.wanted.backend.domain.notice.application.result.NoticeListResult;

public interface NoticeQueryUseCase {
    NoticeListResult getList(GetNoticeListCommand command);
    NoticeDetailResult getDetail(Long noticeId, Long memberId, String role);
}