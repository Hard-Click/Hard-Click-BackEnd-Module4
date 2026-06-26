package com.wanted.backend.domain.identity.infrastructure.persistence;

import com.wanted.backend.domain.identity.domain.model.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

public interface MemberJpaRepository extends JpaRepository<MemberJpaEntity, Long> {
    Optional<MemberJpaEntity> findByUsername(String username);
    Optional<MemberJpaEntity> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByIdAndStatusIn(Long id, Collection<MemberStatus> statuses);

    @Modifying
    @Query("UPDATE MemberJpaEntity m SET m.status = :newStatus, m.updatedAt = :updatedAt " +
           "WHERE m.id = :id AND m.status = com.wanted.backend.domain.identity.domain.model.MemberStatus.ACTIVE")
    int updateStatusIfActive(@Param("id") Long id,
                              @Param("newStatus") MemberStatus newStatus,
                              @Param("updatedAt") LocalDateTime updatedAt);
}
