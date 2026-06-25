package com.wanted.backend.domain.study_timer.domain.repository;

import com.wanted.backend.domain.study_timer.domain.model.DailyStudyStat;

import java.time.LocalDate;
import java.util.List;

public interface DailyStudyStatsRepository {

    DailyStudyStat upsertStudySeconds(
            Long memberId,
            LocalDate studyDate,
            Integer additionalStudySeconds
    );

    List<DailyStudyStat> findByMemberIdAndDateBetween(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate
    );
}
