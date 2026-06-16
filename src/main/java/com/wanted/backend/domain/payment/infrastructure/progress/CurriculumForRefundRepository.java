package com.wanted.backend.domain.payment.infrastructure.progress;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface CurriculumForRefundRepository extends JpaRepository<CurriculumForRefundEntity, Long> {

    List<CurriculumForRefundEntity> findByCourseId(Long courseId);
}
