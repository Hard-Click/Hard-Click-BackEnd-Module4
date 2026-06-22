package com.wanted.backend.domain.grass.domain.repository;

import com.wanted.backend.domain.grass.domain.model.YearlyGrassStat;

import java.time.LocalDate;
import java.util.List;

public interface YearlyGrassRepository {

    List<YearlyGrassStat> findByMemberIdAndDateBetween(Long memberId, LocalDate startDate, LocalDate endDate);
}
