package com.wanted.backend.domain.payment.application.port;

import java.util.List;

public interface OrderItemCreatePort {
    void saveAll(Long orderId, List<Long> courseIds);
}
