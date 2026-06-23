package com.wanted.backend.domain.report_moderation.application.port;

import com.wanted.backend.domain.community.domain.model.TargetType;

public interface AdminContentCommandPort {
    void deleteByAdmin(TargetType contentType, Long contentId);
}
