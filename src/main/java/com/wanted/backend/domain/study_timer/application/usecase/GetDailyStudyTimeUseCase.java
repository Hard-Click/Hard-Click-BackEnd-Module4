package com.wanted.backend.domain.study_timer.application.usecase;

import com.wanted.backend.domain.study_timer.application.query.GetDailyStudyTimeQuery;

import java.time.LocalDate;
import java.util.List;

public interface GetDailyStudyTimeUseCase {

    List<DailyStudyTimeItem> handle(GetDailyStudyTimeQuery query);

    record DailyStudyTimeItem(
            LocalDate date,
            Integer studySeconds
    ) {
    }
}
