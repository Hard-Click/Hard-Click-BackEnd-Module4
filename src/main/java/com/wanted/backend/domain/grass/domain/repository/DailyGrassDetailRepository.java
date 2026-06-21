package com.wanted.backend.domain.grass.domain.repository;

import com.wanted.backend.domain.grass.domain.model.DailyGrassDetailStat;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyGrassDetailRepository {

    Optional<DailyGrassDetailStat> findByMemberIdAndStatDate(Long memberId, LocalDate statDate);
}
