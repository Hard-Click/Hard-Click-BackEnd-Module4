package com.wanted.backend.domain.ranking.application.port;

import java.time.LocalDate;
import java.util.Map;

public interface StudyTimeScoreReader {

    Map<Long, Long> sumStudySecondsByDateBetween(LocalDate startDate, LocalDate endDate);
}
