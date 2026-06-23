package com.wanted.backend.domain.report_moderation.application.usecase;

import com.wanted.backend.domain.community.domain.model.TargetType;
import com.wanted.backend.domain.report_moderation.application.dto.AdminContentResult;

public interface GetAdminContentUseCase {
    AdminContentResult getContent(TargetType contentType, Long contentId);
}
