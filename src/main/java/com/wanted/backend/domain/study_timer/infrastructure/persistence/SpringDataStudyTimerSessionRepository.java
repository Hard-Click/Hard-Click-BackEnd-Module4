package com.wanted.backend.domain.study_timer.infrastructure.persistence;

import com.wanted.backend.domain.study_timer.domain.model.StudyTimerSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface SpringDataStudyTimerSessionRepository extends JpaRepository<StudyTimerSessionJpaEntity, Long> {

    boolean existsByMemberIdAndStatus(Long memberId, StudyTimerSessionStatus status);

    boolean existsByMemberIdAndStatusIn(Long memberId, Collection<StudyTimerSessionStatus> statuses);

    Optional<StudyTimerSessionJpaEntity> findByMemberIdAndStatus(Long memberId, StudyTimerSessionStatus status);

    Optional<StudyTimerSessionJpaEntity> findFirstByMemberIdAndStatusIn(Long memberId, Collection<StudyTimerSessionStatus> statuses);
}
