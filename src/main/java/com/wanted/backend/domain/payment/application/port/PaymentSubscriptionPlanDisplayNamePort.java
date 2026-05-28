package com.wanted.backend.domain.payment.application.port;

import java.util.Collection;
import java.util.Map;

public interface PaymentSubscriptionPlanDisplayNamePort {

    Map<Long, String> findNamesByPlanIds(Collection<Long> planIds);
}
