package com.wanted.backend.domain.study_timer.application.service;

import com.wanted.backend.domain.study_timer.application.command.StartStudyTimerSessionCommand;
import com.wanted.backend.domain.study_timer.application.usecase.StartStudyTimerSessionUseCase;
import com.wanted.backend.domain.study_timer.domain.model.StudyTimerSession;
import com.wanted.backend.domain.study_timer.domain.repository.StudyTimerSessionRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StartStudyTimerSessionService implements StartStudyTimerSessionUseCase {

    private final StudyTimerSessionRepository studyTimerSessionRepository;

    @Override
    @Transactional
    public StudyTimerSessionStartView handle(StartStudyTimerSessionCommand command) {
        if (studyTimerSessionRepository.existsRunningByMemberId(command.memberId())) {
            throw new BusinessException(ErrorCode.STUDY_TIMER_SESSION_ALREADY_RUNNING);
        }

        StudyTimerSession saved = studyTimerSessionRepository.save(StudyTimerSession.start(
                command.memberId(),
                command.startedAt()
        ));

        return new StudyTimerSessionStartView(
                saved.id(),
                saved.status().name(),
                saved.startedAt()
        );
    }
}
