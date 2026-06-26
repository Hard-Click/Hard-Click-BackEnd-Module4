package com.wanted.backend.domain.study_timer.application.service;

import com.wanted.backend.domain.study_timer.application.query.GetCurrentStudyTimerSessionQuery;
import com.wanted.backend.domain.study_timer.application.usecase.GetCurrentStudyTimerSessionUseCase;
import com.wanted.backend.domain.study_timer.domain.model.StudyTimerSession;
import com.wanted.backend.domain.study_timer.domain.repository.StudyTimerSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetCurrentStudyTimerSessionService implements GetCurrentStudyTimerSessionUseCase {

    private final StudyTimerSessionRepository studyTimerSessionRepository;

    @Override
    public CurrentStudyTimerSessionView handle(GetCurrentStudyTimerSessionQuery query) {
        return studyTimerSessionRepository.findActiveByMemberId(query.memberId())
                .map(this::toView)
                .orElse(null);
    }

    private CurrentStudyTimerSessionView toView(StudyTimerSession session) {
        return new CurrentStudyTimerSessionView(
                session.id(),
                session.status().name(),
                session.startedAt(),
                session.accumulatedStudySeconds()
        );
    }
}
