package com.wanted.backend.domain.community.application.usecase;

import com.wanted.backend.domain.community.application.command.CreateReportCommand;

public interface ReportCommandUseCase {
    Long create(CreateReportCommand command);
}