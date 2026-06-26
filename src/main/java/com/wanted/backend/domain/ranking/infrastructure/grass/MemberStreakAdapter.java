package com.wanted.backend.domain.ranking.infrastructure.grass;

import com.wanted.backend.domain.grass.application.query.GetStudyStreakQuery;
import com.wanted.backend.domain.grass.application.usecase.GetStudyStreakUseCase;
import com.wanted.backend.domain.ranking.application.port.MemberStreakPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberStreakAdapter implements MemberStreakPort {

    private final GetStudyStreakUseCase getStudyStreakUseCase;

    @Override
    public int getCurrentStreakDays(Long memberId) {
        return getStudyStreakUseCase.handle(new GetStudyStreakQuery(memberId)).streak();
    }
}
