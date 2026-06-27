package com.wanted.backend.domain.enrollment_management.infrastructure.section;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface EnrolledCourseSectionReferenceRepository extends JpaRepository<EnrolledCourseSectionReferenceEntity, Long> {

    List<EnrolledCourseSectionReferenceEntity> findByCourseIdInOrderByCourseIdAscOrderIndexAsc(Collection<Long> courseIds);
}
