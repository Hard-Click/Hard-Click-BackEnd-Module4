package com.wanted.backend.domain.grass.domain.repository;

import com.wanted.backend.domain.grass.domain.model.StudyTimeGrassStat;

import java.time.LocalDate;
import java.util.List;

public interface StudyTimeGrassRepository {

    List<StudyTimeGrassStat> findByMemberIdAndDateBetween(Long memberId, LocalDate startDate, LocalDate endDate);
}
