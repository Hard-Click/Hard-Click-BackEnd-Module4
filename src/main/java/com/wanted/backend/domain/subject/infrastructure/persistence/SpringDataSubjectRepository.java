package com.wanted.backend.domain.subject.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataSubjectRepository extends JpaRepository<SubjectJpaEntity, Long> {
}
