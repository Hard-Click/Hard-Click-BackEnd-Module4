package com.wanted.backend.domain.notice.application.port;


public interface AdminValidationPort {
    boolean isAdmin(Long memberId);
}