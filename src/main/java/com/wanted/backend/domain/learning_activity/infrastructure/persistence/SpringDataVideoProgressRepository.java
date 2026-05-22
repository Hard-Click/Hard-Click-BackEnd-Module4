package com.wanted.backend.domain.learning_activity.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataVideoProgressRepository extends JpaRepository<VideoProgressJpaEntity, Long> {

    Optional<VideoProgressJpaEntity> findByMemberIdAndVideoId(Long memberId, Long videoId);
}
