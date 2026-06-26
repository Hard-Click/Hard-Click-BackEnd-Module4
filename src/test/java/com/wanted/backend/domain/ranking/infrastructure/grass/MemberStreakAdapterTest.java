package com.wanted.backend.domain.ranking.infrastructure.grass;

import com.wanted.backend.domain.grass.application.query.GetStudyStreakQuery;
import com.wanted.backend.domain.grass.application.usecase.GetStudyStreakUseCase;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MemberStreakAdapterTest {

    private final GetStudyStreakUseCase getStudyStreakUseCase = mock(GetStudyStreakUseCase.class);
    private final MemberStreakAdapter adapter = new MemberStreakAdapter(getStudyStreakUseCase);

    @Test
    void delegatesToGetStudyStreakUseCaseAndUnwrapsStreak() {
        when(getStudyStreakUseCase.handle(new GetStudyStreakQuery(1L)))
                .thenReturn(new GetStudyStreakUseCase.StudyStreakView(7));

        int result = adapter.getCurrentStreakDays(1L);

        assertThat(result).isEqualTo(7);
    }
}
