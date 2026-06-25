package com.wanted.backend.domain.study_timer.application.service;

import com.wanted.backend.domain.study_timer.application.command.PauseStudyTimerSessionCommand;
import com.wanted.backend.domain.study_timer.application.port.MemberLockPort;
import com.wanted.backend.domain.study_timer.application.usecase.PauseStudyTimerSessionUseCase;
import com.wanted.backend.domain.study_timer.domain.model.StudyTimerSession;
import com.wanted.backend.domain.study_timer.domain.model.StudyTimerSessionStatus;
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
public class PauseStudyTimerSessionService implements PauseStudyTimerSessionUseCase {

    private static final String ACTION = "pause";

    private final MemberLockPort memberLockPort;
    private final StudyTimerSessionRepository studyTimerSessionRepository;
    private final StudyTimerSessionMetricRecorder metricRecorder;
    private final Clock clock;

    @Override
    @Transactional
    public StudyTimerSessionPauseView handle(PauseStudyTimerSessionCommand command) {
        String errorCode = "UNKNOWN";
        try {
            OffsetDateTime serverNow = OffsetDateTime.now(clock);
            validatePausedAt(command.pausedAt(), serverNow);

            memberLockPort.lock(command.memberId());

            StudyTimerSession session = studyTimerSessionRepository.findById(command.sessionId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_TIMER_SESSION_NOT_FOUND));

            if (!session.isOwnedBy(command.memberId())) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }

            if (session.status() == StudyTimerSessionStatus.PAUSED) {
                errorCode = null;
                return toView(session, command.pausedAt());
            }

            StudyTimerSession saved = studyTimerSessionRepository.save(session.pause(command.pausedAt(), serverNow));

            errorCode = null;
            return toView(saved, command.pausedAt());
        } catch (BusinessException e) {
            errorCode = e.getErrorCode().name();
            throw e;
        } finally {
            recordMetric(errorCode);
        }
    }

    private void recordMetric(String errorCode) {
        if (errorCode == null) {
            metricRecorder.recordSuccess(ACTION);
        } else {
            metricRecorder.recordFailure(ACTION, errorCode);
        }
    }

    private StudyTimerSessionPauseView toView(StudyTimerSession session, OffsetDateTime pausedAt) {
        return new StudyTimerSessionPauseView(
                session.id(),
                session.status().name(),
                session.accumulatedStudySeconds(),
                pausedAt
        );
    }

    private void validatePausedAt(OffsetDateTime pausedAt, OffsetDateTime serverNow) {
        if (pausedAt == null) {
            throw new BusinessException(ErrorCode.STUDY_TIMER_PAUSED_AT_REQUIRED);
        }
        if (pausedAt.toInstant().isAfter(serverNow.toInstant())) {
            throw new BusinessException(ErrorCode.STUDY_TIMER_PAUSED_AT_IN_FUTURE);
        }
    }
}
