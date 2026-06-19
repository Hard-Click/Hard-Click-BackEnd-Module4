package com.wanted.backend.domain.grass.domain.repository;

import com.wanted.backend.domain.grass.domain.model.LessonGrassStat;

import java.time.LocalDate;
import java.util.List;

public interface LessonGrassRepository {

    List<LessonGrassStat> findByMemberIdAndDateBetween(Long memberId, LocalDate startDate, LocalDate endDate);
}
