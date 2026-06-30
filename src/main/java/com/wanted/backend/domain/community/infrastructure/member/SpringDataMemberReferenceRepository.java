package com.wanted.backend.domain.community.infrastructure.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface SpringDataMemberReferenceRepository
        extends JpaRepository<MemberReferenceEntity, Long> {

    @Query("SELECT m FROM MemberReferenceEntity m WHERE m.id IN :ids")
    List<MemberReferenceEntity> findByIdIn(@Param("ids") Collection<Long> ids);
}