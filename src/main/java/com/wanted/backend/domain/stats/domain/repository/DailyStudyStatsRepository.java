package com.wanted.backend.domain.stats.domain.repository;

import com.wanted.backend.domain.stats.domain.model.DailyStudyStat;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyStudyStatsRepository {

    Optional<DailyStudyStat> findByMemberIdAndStatDate(Long memberId, LocalDate statDate);
}
