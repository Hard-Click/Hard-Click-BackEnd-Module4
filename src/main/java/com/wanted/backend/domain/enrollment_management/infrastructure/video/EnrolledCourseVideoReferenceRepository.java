package com.wanted.backend.domain.enrollment_management.infrastructure.video;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface EnrolledCourseVideoReferenceRepository extends JpaRepository<EnrolledCourseVideoReferenceEntity, Long> {

    List<EnrolledCourseVideoReferenceEntity> findByCurriculumIdInOrderByCurriculumIdAscIdAsc(Collection<Long> curriculumIds);
}
