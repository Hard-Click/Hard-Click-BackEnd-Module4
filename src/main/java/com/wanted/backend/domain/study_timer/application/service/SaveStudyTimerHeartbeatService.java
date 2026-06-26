package com.wanted.backend.domain.study_timer.application.service;

import com.wanted.backend.domain.study_timer.application.command.SaveStudyTimerHeartbeatCommand;
import com.wanted.backend.domain.study_timer.application.port.MemberLockPort;
import com.wanted.backend.domain.study_timer.application.usecase.SaveStudyTimerHeartbeatUseCase;
import com.wanted.backend.domain.study_timer.domain.model.StudyTimerSession;
import com.wanted.backend.domain.study_timer.domain.repository.StudyTimerSessionRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SaveStudyTimerHeartbeatService implements SaveStudyTimerHeartbeatUseCase {

    private static final StudyTimerAction ACTION = StudyTimerAction.HEARTBEAT;

    private final MemberLockPort memberLockPort;
    private final StudyTimerSessionRepository studyTimerSessionRepository;
    private final StudyTimerSessionMetricRecorder metricRecorder;
    private final Clock clock;

    @Override
    @Transactional
    public StudyTimerHeartbeatView handle(SaveStudyTimerHeartbeatCommand command) {
        String errorCode = "UNKNOWN";
        try {
            OffsetDateTime serverNow = OffsetDateTime.now(clock);
            validateHeartbeatAt(command.heartbeatAt(), serverNow);

            memberLockPort.lock(command.memberId());

            StudyTimerSession session = studyTimerSessionRepository.findById(command.sessionId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_TIMER_SESSION_NOT_FOUND));

            if (!session.isOwnedBy(command.memberId())) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }

            StudyTimerSession saved = studyTimerSessionRepository.save(session.heartbeat(command.heartbeatAt(), serverNow));
            Integer accumulatedStudySeconds = saved.accumulatedStudySeconds();

            errorCode = null;
            return new StudyTimerHeartbeatView(
                    saved.id(),
                    saved.status().name(),
                    accumulatedStudySeconds,
                    command.heartbeatAt()
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

    private void validateHeartbeatAt(OffsetDateTime heartbeatAt, OffsetDateTime serverNow) {
        if (heartbeatAt == null) {
            throw new BusinessException(ErrorCode.STUDY_TIMER_HEARTBEAT_AT_REQUIRED);
        }
        if (heartbeatAt.toInstant().isAfter(serverNow.toInstant())) {
            throw new BusinessException(ErrorCode.STUDY_TIMER_HEARTBEAT_AT_IN_FUTURE);
        }
    }
}
