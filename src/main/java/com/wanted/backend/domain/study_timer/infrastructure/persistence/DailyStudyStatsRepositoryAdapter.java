package com.wanted.backend.domain.study_timer.infrastructure.persistence;

import com.wanted.backend.domain.study_timer.domain.model.DailyStudyStat;
import com.wanted.backend.domain.study_timer.domain.repository.DailyStudyStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyStudyStatsRepositoryAdapter implements DailyStudyStatsRepository {

    private final SpringDataDailyStudyStatsRepository repository;
    private final Clock clock;

    @Override
    @Transactional
    public DailyStudyStat upsertStudySeconds(
            Long memberId,
            LocalDate studyDate,
            Integer additionalStudySeconds
    ) {
        DailyStudyStat additionalStat = new DailyStudyStat(memberId, studyDate, additionalStudySeconds);
        LocalDateTime now = LocalDateTime.now(clock);
        DailyStudyStatsJpaEntity entity = repository.findByMemberIdAndStatDate(memberId, studyDate)
                .orElseGet(() -> new DailyStudyStatsJpaEntity(
                        memberId,
                        studyDate,
                        0,
                        now,
                        now
                ));

        DailyStudyStat updatedStat = toDomain(entity).increaseStudySeconds(additionalStat.studySeconds());
        entity.updateStudySeconds(updatedStat.studySeconds(), now);

        return toDomain(repository.saveAndFlush(entity));
    }

    @Override
    public List<DailyStudyStat> findByMemberIdAndDateBetween(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return repository.findByMemberIdAndStatDateBetweenOrderByStatDateAsc(memberId, startDate, endDate).stream()
                .map(this::toDomain)
                .toList();
    }

    private DailyStudyStat toDomain(DailyStudyStatsJpaEntity entity) {
        return new DailyStudyStat(
                entity.getMemberId(),
                entity.getStatDate(),
                entity.getStudySeconds()
        );
    }
}
