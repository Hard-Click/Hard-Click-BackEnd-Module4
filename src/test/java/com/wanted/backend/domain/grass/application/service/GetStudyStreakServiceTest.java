package com.wanted.backend.domain.grass.application.service;

import com.wanted.backend.domain.grass.application.query.GetStudyStreakQuery;
import com.wanted.backend.domain.grass.application.usecase.GetStudyStreakUseCase.StudyStreakView;
import com.wanted.backend.domain.grass.domain.model.StudyStreakStat;
import com.wanted.backend.domain.grass.domain.policy.GrassLearningStatusPolicy;
import com.wanted.backend.domain.grass.domain.policy.StudyStreakPolicy;
import com.wanted.backend.domain.grass.domain.repository.StudyStreakRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetStudyStreakServiceTest {

    private final StudyStreakRepository repository = mock(StudyStreakRepository.class);
    private final StudyStreakPolicy policy = new StudyStreakPolicy(new GrassLearningStatusPolicy());

    @Test
    void returnsCurrentStudyStreakDays() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-06-20T00:00:00Z"),
                ZoneId.of("Asia/Seoul")
        );
        GetStudyStreakService service = new GetStudyStreakService(repository, policy, clock);
        LocalDate today = LocalDate.parse("2026-06-20");
        when(repository.findByMemberIdAndDateLessThanEqual(1L, today))
                .thenReturn(List.of(
                        new StudyStreakStat(today, 1, 0),
                        new StudyStreakStat(LocalDate.parse("2026-06-19"), 0, 3600)
                ));

        StudyStreakView result = service.handle(new GetStudyStreakQuery(1L));

        assertThat(result.streak()).isEqualTo(2);
        verify(repository).findByMemberIdAndDateLessThanEqual(1L, today);
    }

    @Test
    void usesClockZoneForToday() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-06-19T15:30:00Z"),
                ZoneId.of("Asia/Seoul")
        );
        GetStudyStreakService service = new GetStudyStreakService(repository, policy, clock);
        LocalDate kstToday = LocalDate.parse("2026-06-20");
        when(repository.findByMemberIdAndDateLessThanEqual(1L, kstToday))
                .thenReturn(List.of(new StudyStreakStat(kstToday, 1, 0)));

        StudyStreakView result = service.handle(new GetStudyStreakQuery(1L));

        assertThat(result.streak()).isEqualTo(1);
        verify(repository).findByMemberIdAndDateLessThanEqual(1L, kstToday);
    }
}
