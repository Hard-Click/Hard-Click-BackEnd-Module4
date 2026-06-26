package com.wanted.backend.domain.report_moderation.application.service;

import com.wanted.backend.domain.community.domain.model.TargetType;
import com.wanted.backend.domain.report_moderation.application.dto.AdminContentResult;
import com.wanted.backend.domain.report_moderation.application.policy.AdminContentDisplayPolicy;
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
    private final AdminContentDisplayPolicy adminContentDisplayPolicy;

    @Override
    public AdminContentResult getContent(TargetType contentType, Long contentId) {
        AdminContentResult result = adminContentQueryPort.findContent(contentType, contentId)
                .orElseThrow(() -> new BusinessException(notFoundErrorCode(contentType)));

        return new AdminContentResult(
                result.contentType(),
                result.contentId(),
                result.title(),
                adminContentDisplayPolicy.resolveContent(
                        result.contentType(), result.status(), result.content()
                ),
                result.status()
        );
    }

    private ErrorCode notFoundErrorCode(TargetType contentType) {
        return switch (contentType) {
            case POST -> ErrorCode.POST_NOT_FOUND;
            case COMMENT -> ErrorCode.COMMENT_NOT_FOUND;
            case REVIEW -> ErrorCode.REVIEW_NOT_FOUND;
        };
    }
}
