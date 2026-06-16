package com.wanted.backend.domain.payment.infrastructure.progress;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

interface VideoForRefundRepository extends JpaRepository<VideoForRefundEntity, Long> {

    long countByCurriculumIdIn(Collection<Long> curriculumIds);
}
