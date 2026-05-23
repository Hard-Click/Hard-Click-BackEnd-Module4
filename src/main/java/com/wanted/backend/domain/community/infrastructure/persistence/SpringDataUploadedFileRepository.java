package com.wanted.backend.domain.community.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataUploadedFileRepository
        extends JpaRepository<UploadedFileJpaEntity, Long> {}