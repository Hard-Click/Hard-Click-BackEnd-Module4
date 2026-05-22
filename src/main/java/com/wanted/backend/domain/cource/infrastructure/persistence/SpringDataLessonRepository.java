package com.wanted.backend.domain.cource.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataLessonRepository extends JpaRepository<LessonJpaEntity, Long> {
}
