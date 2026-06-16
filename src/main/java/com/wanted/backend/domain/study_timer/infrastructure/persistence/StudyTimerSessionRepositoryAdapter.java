package com.wanted.backend.domain.study_timer.infrastructure.persistence;

import com.wanted.backend.domain.study_timer.domain.model.StudyTimerSession;
import com.wanted.backend.domain.study_timer.domain.model.StudyTimerSessionStatus;
import com.wanted.backend.domain.study_timer.domain.repository.StudyTimerSessionRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyTimerSessionRepositoryAdapter implements StudyTimerSessionRepository {

    private final SpringDataStudyTimerSessionRepository repository;
    private final Clock clock;

    @Override
    public boolean existsRunningByMemberId(Long memberId) {
        return repository.existsByMemberIdAndStatus(memberId, StudyTimerSessionStatus.RUNNING);
    }

    @Override
    @Transactional
    public StudyTimerSession save(StudyTimerSession session) {
        LocalDateTime now = LocalDateTime.now(clock);
        StudyTimerSessionJpaEntity entity = new StudyTimerSessionJpaEntity(
                session.memberId(),
                session.courseId(),
                session.lessonId(),
                toLocalDateTime(session.startedAt()),
                toLocalDateTime(session.endedAt()),
                session.elapsedSeconds(),
                session.status(),
                now,
                now
        );

        try {
            return toDomain(repository.saveAndFlush(entity));
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(ErrorCode.STUDY_TIMER_SESSION_ALREADY_RUNNING);
        }
    }

    private StudyTimerSession toDomain(StudyTimerSessionJpaEntity entity) {
        return new StudyTimerSession(
                entity.getId(),
                entity.getMemberId(),
                entity.getCourseId(),
                entity.getLessonId(),
                toOffsetDateTime(entity.getStartedAt()),
                toOffsetDateTime(entity.getEndedAt()),
                entity.getElapsedSeconds(),
                entity.getStatus()
        );
    }

    private LocalDateTime toLocalDateTime(OffsetDateTime dateTime) {
        return dateTime == null ? null : dateTime.atZoneSameInstant(clock.getZone()).toLocalDateTime();
    }

    private OffsetDateTime toOffsetDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atZone(clock.getZone()).toOffsetDateTime();
    }
}
