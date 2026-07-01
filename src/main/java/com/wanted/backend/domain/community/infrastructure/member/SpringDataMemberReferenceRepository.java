package com.wanted.backend.domain.community.infrastructure.member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SpringDataMemberReferenceRepository
        extends JpaRepository<MemberReferenceEntity, Long> {

    // 방법②: Batch IN 조회
    List<MemberReferenceEntity> findByIdIn(Collection<Long> ids);
}