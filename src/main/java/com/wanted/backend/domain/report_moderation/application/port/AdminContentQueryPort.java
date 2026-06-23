package com.wanted.backend.domain.report_moderation.application.port;

import com.wanted.backend.domain.community.domain.model.TargetType;
import com.wanted.backend.domain.report_moderation.application.dto.AdminContentResult;

import java.util.Optional;

public interface AdminContentQueryPort {
    Optional<AdminContentResult> findContent(TargetType contentType, Long contentId);
}
