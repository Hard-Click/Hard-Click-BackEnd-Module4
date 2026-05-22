package com.wanted.backend.domain.community.infrastructure.member;

import org.springframework.data.jpa.repository.JpaRepository;


public interface SpringDataMemberReferenceRepository
        extends JpaRepository<MemberReferenceEntity, Long> {}