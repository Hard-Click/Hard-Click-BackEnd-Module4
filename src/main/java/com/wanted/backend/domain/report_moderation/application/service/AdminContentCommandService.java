package com.wanted.backend.domain.report_moderation.application.service;

import com.wanted.backend.domain.report_moderation.application.command.ChangeAdminContentStatusCommand;
import com.wanted.backend.domain.report_moderation.application.dto.AdminContentStatusResult;
import com.wanted.backend.domain.report_moderation.application.policy.AdminContentStatus;
import com.wanted.backend.domain.report_moderation.application.port.AdminContentCommandPort;
import com.wanted.backend.domain.report_moderation.application.usecase.ChangeAdminContentStatusUseCase;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminContentCommandService implements ChangeAdminContentStatusUseCase {

    private final AdminContentCommandPort adminContentCommandPort;

    @Override
    public AdminContentStatusResult changeStatus(ChangeAdminContentStatusCommand command) {
        if (!AdminContentStatus.ADMIN_DELETED.equals(command.status())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        adminContentCommandPort.deleteByAdmin(command.contentType(), command.contentId());

        return new AdminContentStatusResult(
                command.contentType(),
                command.contentId(),
                AdminContentStatus.ADMIN_DELETED
        );
    }
}
