package com.wanted.backend.domain.identity.domain.repository;

import com.wanted.backend.domain.identity.domain.model.Member;
import com.wanted.backend.domain.identity.domain.model.MemberStatus;
import java.time.LocalDateTime;
import java.util.Optional;

public interface MemberRepository {
    Optional<Member> findById(Long id);
    Optional<Member> findByUsername(String username);
    Optional<Member> findByEmail(String email);
    Member save(Member member);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    int updateStatusIfActive(Long memberId, MemberStatus newStatus, LocalDateTime updatedAt);
}