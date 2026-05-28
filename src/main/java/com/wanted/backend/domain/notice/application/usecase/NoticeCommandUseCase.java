package com.wanted.backend.domain.notice.application.usecase;

import com.wanted.backend.domain.notice.application.command.CreateNoticeCommand;


public interface NoticeCommandUseCase {
    Long create(CreateNoticeCommand command);
}