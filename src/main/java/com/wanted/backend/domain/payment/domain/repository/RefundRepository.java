package com.wanted.backend.domain.payment.domain.repository;

import com.wanted.backend.domain.payment.domain.model.Refund;

public interface RefundRepository {
    Refund save(Refund refund);
}
