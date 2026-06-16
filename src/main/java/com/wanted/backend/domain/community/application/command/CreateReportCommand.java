package com.wanted.backend.domain.community.application.command;

import com.wanted.backend.domain.community.domain.model.ReportType;
import com.wanted.backend.domain.community.domain.model.TargetType;

import java.util.List;

public record CreateReportCommand(
        Long reporterId,
        TargetType targetType,
        Long targetId,
        List<ReportType> reportTypes,
        String reason
) {}