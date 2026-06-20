package com.wanted.backend.domain.grass.domain.repository;

import com.wanted.backend.domain.grass.domain.model.StudyStreakStat;

import java.time.LocalDate;
import java.util.List;

public interface StudyStreakRepository {

    List<StudyStreakStat> findByMemberIdAndDateLessThanEqual(
            Long memberId,
            LocalDate endDate,
            int pageNumber,
            int pageSize
    );
}
