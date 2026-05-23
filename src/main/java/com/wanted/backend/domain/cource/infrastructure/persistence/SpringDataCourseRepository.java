package com.wanted.backend.domain.cource.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface SpringDataCourseRepository extends JpaRepository<CourseJpaEntity, Long>,
        JpaSpecificationExecutor<CourseJpaEntity> {
}
