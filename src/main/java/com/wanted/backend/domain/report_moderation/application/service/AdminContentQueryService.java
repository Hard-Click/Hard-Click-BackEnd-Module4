package com.wanted.backend.domain.report_moderation.application.service;

import com.wanted.backend.domain.community.domain.model.TargetType;
import com.wanted.backend.domain.report_moderation.application.dto.AdminContentResult;
import com.wanted.backend.domain.report_moderation.application.port.AdminContentQueryPort;
import com.wanted.backend.domain.report_moderation.application.usecase.GetAdminContentUseCase;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminContentQueryService implements GetAdminContentUseCase {

    private final AdminContentQueryPort adminContentQueryPort;

    @Override
    public AdminContentResult getContent(TargetType contentType, Long contentId) {
        return adminContentQueryPort.findContent(contentType, contentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPORT_TARGET_NOT_FOUND));
    }
}
