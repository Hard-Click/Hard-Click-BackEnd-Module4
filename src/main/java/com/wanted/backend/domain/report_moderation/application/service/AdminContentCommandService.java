package com.wanted.backend.domain.report_moderation.application.service;

import com.wanted.backend.domain.report_moderation.application.command.AdminContentStatusCommand;
import com.wanted.backend.domain.report_moderation.application.dto.AdminContentResult;
import com.wanted.backend.domain.report_moderation.application.port.AdminContentCommandPort;
import com.wanted.backend.domain.report_moderation.application.usecase.ChangeAdminContentStatusUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminContentCommandService implements ChangeAdminContentStatusUseCase {

    private final AdminContentCommandPort adminContentCommandPort;

    @Override
    @Transactional
    @CacheEvict(cacheNames = "adminDashboard", key = "'summary'")
    public AdminContentResult changeStatus(AdminContentStatusCommand command) {
        return adminContentCommandPort.changeStatus(command);
    }
}
