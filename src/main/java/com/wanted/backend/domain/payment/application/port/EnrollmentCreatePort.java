package com.wanted.backend.domain.payment.application.port;

import java.util.List;

public interface EnrollmentCreatePort {
    void createAll(Long memberId, List<Long> courseIds);
}
