package com.wanted.backend.domain.study_timer.application.service;

import com.wanted.backend.domain.study_timer.application.command.ResumeStudyTimerSessionCommand;
import com.wanted.backend.domain.study_timer.application.port.MemberLockPort;
import com.wanted.backend.domain.study_timer.application.usecase.ResumeStudyTimerSessionUseCase;
import com.wanted.backend.domain.study_timer.domain.model.StudyTimerSession;
import com.wanted.backend.domain.study_timer.domain.repository.StudyTimerSessionRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResumeStudyTimerSessionService implements ResumeStudyTimerSessionUseCase {

    private static final StudyTimerAction ACTION = StudyTimerAction.RESUME;

    private final MemberLockPort memberLockPort;
    private final StudyTimerSessionRepository studyTimerSessionRepository;
    private final StudyTimerSessionMetricRecorder metricRecorder;
    private final Clock clock;

    @Override
    @Transactional
    public StudyTimerSessionResumeView handle(ResumeStudyTimerSessionCommand command) {
        String errorCode = "UNKNOWN";
        try {
            OffsetDateTime serverNow = OffsetDateTime.now(clock);
            validateResumedAt(command.resumedAt(), serverNow);

            memberLockPort.lock(command.memberId());

            StudyTimerSession session = studyTimerSessionRepository.findById(command.sessionId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_TIMER_SESSION_NOT_FOUND));

            if (!session.isOwnedBy(command.memberId())) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }

            StudyTimerSession saved = studyTimerSessionRepository.save(session.resume(command.resumedAt(), serverNow));

            errorCode = null;
            return new StudyTimerSessionResumeView(
                    saved.id(),
                    saved.status().name(),
                    saved.accumulatedStudySeconds(),
                    command.resumedAt()
            );
        } catch (BusinessException e) {
            errorCode = e.getErrorCode().name();
            throw e;
        } finally {
            try {
                metricRecorder.recordResult(ACTION, errorCode);
            } catch (RuntimeException ignored) {
                // metric failure must not affect the business transaction
            }
        }
    }

    private void validateResumedAt(OffsetDateTime resumedAt, OffsetDateTime serverNow) {
        if (resumedAt == null) {
            throw new BusinessException(ErrorCode.STUDY_TIMER_RESUMED_AT_REQUIRED);
        }
        if (resumedAt.toInstant().isAfter(serverNow.toInstant())) {
            throw new BusinessException(ErrorCode.STUDY_TIMER_RESUMED_AT_IN_FUTURE);
        }
    }
}
