package com.wanted.backend.domain.study_timer.application.service;

import com.wanted.backend.domain.study_timer.application.command.EndStudyTimerSessionCommand;
import com.wanted.backend.domain.study_timer.application.port.MemberLockPort;
import com.wanted.backend.domain.study_timer.application.usecase.EndStudyTimerSessionUseCase;
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
public class EndStudyTimerSessionService implements EndStudyTimerSessionUseCase {

    private final MemberLockPort memberLockPort;
    private final StudyTimerSessionRepository studyTimerSessionRepository;
    private final Clock clock;

    @Override
    @Transactional
    public StudyTimerSessionEndView handle(EndStudyTimerSessionCommand command) {
        OffsetDateTime serverNow = OffsetDateTime.now(clock);
        validateEndedAt(command.endedAt(), serverNow);

        memberLockPort.lock(command.memberId());

        StudyTimerSession session = studyTimerSessionRepository.findById(command.sessionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_TIMER_SESSION_NOT_FOUND));

        if (!session.isOwnedBy(command.memberId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        StudyTimerSession saved = studyTimerSessionRepository.save(session.end(command.endedAt(), serverNow));

        return new StudyTimerSessionEndView(
                saved.id(),
                saved.accumulatedStudySeconds(),
                saved.status().name()
        );
    }

    private void validateEndedAt(OffsetDateTime endedAt, OffsetDateTime serverNow) {
        if (endedAt == null) {
            throw new BusinessException(ErrorCode.STUDY_TIMER_ENDED_AT_REQUIRED);
        }
        if (endedAt.toInstant().isAfter(serverNow.toInstant())) {
            throw new BusinessException(ErrorCode.STUDY_TIMER_ENDED_AT_IN_FUTURE);
        }
    }
}
