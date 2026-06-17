package com.wanted.backend.domain.study_timer.application.service;

import com.wanted.backend.domain.study_timer.application.command.SaveStudyTimerHeartbeatCommand;
import com.wanted.backend.domain.study_timer.application.port.MemberLockPort;
import com.wanted.backend.domain.study_timer.application.usecase.SaveStudyTimerHeartbeatUseCase;
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
public class SaveStudyTimerHeartbeatService implements SaveStudyTimerHeartbeatUseCase {

    private final MemberLockPort memberLockPort;
    private final StudyTimerSessionRepository studyTimerSessionRepository;
    private final Clock clock;

    @Override
    @Transactional
    public StudyTimerHeartbeatView handle(SaveStudyTimerHeartbeatCommand command) {
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

        return new StudyTimerHeartbeatView(
                saved.id(),
                saved.status().name(),
                accumulatedStudySeconds,
                command.heartbeatAt()
        );
    }

    private void validateHeartbeatAt(OffsetDateTime heartbeatAt, OffsetDateTime serverNow) {
        if (heartbeatAt == null) {
            throw new IllegalArgumentException("하트비트 시각은 필수입니다.");
        }
        if (heartbeatAt.toInstant().isAfter(serverNow.toInstant())) {
            throw new IllegalArgumentException("하트비트 시각은 현재 시각 이후일 수 없습니다.");
        }
    }
}
