package com.wanted.backend.domain.community.application.port;

public interface MemberAutoSuspendPort {
    void suspendForReportThreshold(Long memberId);
}