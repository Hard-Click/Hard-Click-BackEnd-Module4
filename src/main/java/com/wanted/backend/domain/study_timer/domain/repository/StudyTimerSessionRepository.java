package com.wanted.backend.domain.study_timer.domain.repository;

import com.wanted.backend.domain.study_timer.domain.model.StudyTimerSession;

public interface StudyTimerSessionRepository {

    boolean existsRunningByMemberId(Long memberId);

    StudyTimerSession save(StudyTimerSession session);
}
