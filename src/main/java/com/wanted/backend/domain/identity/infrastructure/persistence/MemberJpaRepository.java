package com.wanted.backend.domain.identity.infrastructure.persistence;

import com.wanted.backend.domain.identity.domain.model.MemberStatus;
import com.wanted.backend.domain.identity.domain.model.Role;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberJpaRepository extends JpaRepository<MemberJpaEntity, Long> {
    Optional<MemberJpaEntity> findByUsername(String username);
    Optional<MemberJpaEntity> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("""
            select m
            from MemberJpaEntity m
            where (:keyword is null
                or lower(m.name) like lower(concat('%', :keyword, '%'))
                or lower(m.username) like lower(concat('%', :keyword, '%'))
                or lower(m.email) like lower(concat('%', :keyword, '%')))
              and (:role is null or m.role = :role)
              and (:status is null or m.status = :status)
            order by m.createdAt desc, m.id desc
            """)
    Slice<MemberJpaEntity> searchAdminMembers(
            @Param("keyword") String keyword,
            @Param("role") Role role,
            @Param("status") MemberStatus status,
            Pageable pageable
    );
}
