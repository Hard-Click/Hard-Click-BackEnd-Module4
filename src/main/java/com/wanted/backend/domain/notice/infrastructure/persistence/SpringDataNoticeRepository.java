package com.wanted.backend.domain.notice.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataNoticeRepository
        extends JpaRepository<NoticeJpaEntity, Long> {}