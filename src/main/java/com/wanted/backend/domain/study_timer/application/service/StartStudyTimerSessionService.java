package com.wanted.backend.domain.study_timer.application.service;

import com.wanted.backend.domain.study_timer.application.command.StartStudyTimerSessionCommand;
import com.wanted.backend.domain.study_timer.application.port.MemberLockPort;
import com.wanted.backend.domain.study_timer.application.usecase.StartStudyTimerSessionUseCase;
import com.wanted.backend.domain.study_timer.domain.model.StudyTimerSession;
import com.wanted.backend.domain.study_timer.domain.repository.StudyTimerSessionRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StartStudyTimerSessionService implements StartStudyTimerSessionUseCase {

    private static final StudyTimerAction ACTION = StudyTimerAction.START;

    private final MemberLockPort memberLockPort;
    private final StudyTimerSessionRepository studyTimerSessionRepository;
    private final StudyTimerSessionMetricRecorder metricRecorder;

    @Override
    @Transactional
    public StudyTimerSessionStartView handle(StartStudyTimerSessionCommand command) {
        String errorCode = "UNKNOWN";
        try {
            memberLockPort.lock(command.memberId());

            if (studyTimerSessionRepository.existsActiveByMemberId(command.memberId())) {
                throw new BusinessException(ErrorCode.STUDY_TIMER_SESSION_ALREADY_RUNNING);
            }

            StudyTimerSession saved = studyTimerSessionRepository.save(StudyTimerSession.start(
                    command.memberId(),
                    command.startedAt()
            ));

            errorCode = null;
            return new StudyTimerSessionStartView(
                    saved.id(),
                    saved.status().name(),
                    saved.startedAt()
            );
        } catch (BusinessException e) {
            errorCode = e.getErrorCode().name();
            throw e;
        } finally {
            try {
                metricRecorder.recordResult(ACTION, errorCode);
            } catch (RuntimeException e) {
                // metric failure must not affect the business transaction
                log.warn("study timer metric record failed: action={}, errorCode={}", ACTION, errorCode, e);
            }
        }
    }
}
