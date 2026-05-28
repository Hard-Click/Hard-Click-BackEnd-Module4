package com.wanted.backend.domain.payment.infrastructure.course;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PaymentCourseReferenceRepository extends JpaRepository<PaymentCourseReferenceEntity, Long> {

    List<PaymentCourseReferenceEntity> findByIdIn(Collection<Long> courseIds);
}
