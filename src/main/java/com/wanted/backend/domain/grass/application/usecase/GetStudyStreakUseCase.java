package com.wanted.backend.domain.grass.application.usecase;

import com.wanted.backend.domain.grass.application.query.GetStudyStreakQuery;

public interface GetStudyStreakUseCase {

    StudyStreakView handle(GetStudyStreakQuery query);

    record StudyStreakView(
            Integer streak
    ) {
    }
}
