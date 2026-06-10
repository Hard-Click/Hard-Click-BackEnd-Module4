package com.wanted.backend.domain.notice.infrastructure.member;

import org.springframework.data.jpa.repository.JpaRepository;


public interface SpringDataNoticeMemberReferenceRepository
        extends JpaRepository<MemberReferenceEntity, Long> {}