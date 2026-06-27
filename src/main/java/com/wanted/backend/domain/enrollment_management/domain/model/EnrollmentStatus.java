package com.wanted.backend.domain.enrollment_management.domain.model;

import java.util.EnumSet;
import java.util.Set;

public enum EnrollmentStatus {
    ENROLLED,
    IN_PROGRESS,
    COMPLETED,
    EXPIRED,
    REFUNDED;

    public static Set<EnrollmentStatus> myCourseListTargets() {
        return EnumSet.of(IN_PROGRESS, COMPLETED);
    }

    /** 유효한 수강 권한 보유 상태(재구매 차단 대상). EXPIRED/REFUNDED는 재구매 허용. */
    public boolean isActive() {
        return this == ENROLLED || this == IN_PROGRESS || this == COMPLETED;
    }
}
