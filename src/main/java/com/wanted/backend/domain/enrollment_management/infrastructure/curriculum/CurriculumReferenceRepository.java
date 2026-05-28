package com.wanted.backend.domain.enrollment_management.infrastructure.curriculum;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CurriculumReferenceRepository extends JpaRepository<CurriculumReferenceEntity, Long> {

    List<CurriculumReferenceEntity> findByCourseIdInOrderByCourseIdAscIdAsc(Collection<Long> courseIds);
}
