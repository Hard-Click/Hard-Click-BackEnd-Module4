package com.wanted.backend.domain.study_timer.domain.repository;

import com.wanted.backend.domain.study_timer.domain.model.StudyTimerSession;

import java.util.Optional;

public interface StudyTimerSessionRepository {

    boolean existsRunningByMemberId(Long memberId);

    Optional<StudyTimerSession> findById(Long sessionId);

    StudyTimerSession save(StudyTimerSession session);
}
