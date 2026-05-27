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
        return EnumSet.of(ENROLLED, IN_PROGRESS, COMPLETED);
    }
}
