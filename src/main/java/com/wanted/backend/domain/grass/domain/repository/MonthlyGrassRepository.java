package com.wanted.backend.domain.grass.domain.repository;

import com.wanted.backend.domain.grass.domain.model.MonthlyGrassStat;

import java.time.LocalDate;
import java.util.List;

public interface MonthlyGrassRepository {

    List<MonthlyGrassStat> findByMemberIdAndDateBetween(Long memberId, LocalDate startDate, LocalDate endDate);
}
