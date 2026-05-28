package com.wanted.backend.domain.payment.application.port;

import java.util.Collection;
import java.util.Map;

public interface PaymentCourseDisplayNamePort {

    Map<Long, String> findNamesByCourseIds(Collection<Long> courseIds);
}
