package com.wanted.backend.domain.grass.application.service;

import com.wanted.backend.domain.grass.application.query.GetStudyTimeGrassQuery;
import com.wanted.backend.domain.grass.application.usecase.GetStudyTimeGrassUseCase;
import com.wanted.backend.domain.grass.domain.model.StudyTimeGrassStat;
import com.wanted.backend.domain.grass.domain.policy.StudyTimeGrassLevelPolicy;
import com.wanted.backend.domain.grass.domain.repository.StudyTimeGrassRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetStudyTimeGrassServiceTest {

    private StudyTimeGrassRepository repository;
    private GetStudyTimeGrassService service;

    @BeforeEach
    void setUp() {
        repository = mock(StudyTimeGrassRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-01-03T00:00:00Z"), ZoneId.of("Asia/Seoul"));
        service = new GetStudyTimeGrassService(
                repository,
                new StudyTimeGrassLevelPolicy(List.of(1, 1800, 3600, 7200)),
                clock
        );
    }

    @Test
    void returnsYearlyStudyTimeGrassWithZeroFilledFutureDates() {
        when(repository.findByMemberIdAndDateBetween(
                1L,
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-01-03")
        )).thenReturn(List.of(
                new StudyTimeGrassStat(1L, LocalDate.parse("2026-01-01"), 1200),
                new StudyTimeGrassStat(1L, LocalDate.parse("2026-01-03"), 8000)
        ));

        List<GetStudyTimeGrassUseCase.StudyTimeGrassView> result =
                service.handle(new GetStudyTimeGrassQuery(1L));

        assertThat(result).hasSize(365);
        assertThat(result.subList(0, 4))
                .extracting(
                        GetStudyTimeGrassUseCase.StudyTimeGrassView::date,
                        GetStudyTimeGrassUseCase.StudyTimeGrassView::studySeconds,
                        GetStudyTimeGrassUseCase.StudyTimeGrassView::level,
                        GetStudyTimeGrassUseCase.StudyTimeGrassView::isFuture
                )
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(LocalDate.parse("2026-01-01"), 1200, 1, false),
                        org.assertj.core.groups.Tuple.tuple(LocalDate.parse("2026-01-02"), 0, 0, false),
                        org.assertj.core.groups.Tuple.tuple(LocalDate.parse("2026-01-03"), 8000, 4, false),
                        org.assertj.core.groups.Tuple.tuple(LocalDate.parse("2026-01-04"), 0, 0, true)
                );
        assertThat(result.get(364).date()).isEqualTo(LocalDate.parse("2026-12-31"));
        assertThat(result.get(364).isFuture()).isTrue();
    }

    @Test
    void returnsLeapYearStudyTimeGrassWith366Days() {
        Clock leapYearClock = Clock.fixed(Instant.parse("2024-12-31T00:00:00Z"), ZoneId.of("Asia/Seoul"));
        GetStudyTimeGrassService leapYearService = new GetStudyTimeGrassService(
                repository,
                new StudyTimeGrassLevelPolicy(List.of(1, 1800, 3600, 7200)),
                leapYearClock
        );

        when(repository.findByMemberIdAndDateBetween(
                1L,
                LocalDate.parse("2024-01-01"),
                LocalDate.parse("2024-12-31")
        )).thenReturn(List.of(
                new StudyTimeGrassStat(1L, LocalDate.parse("2024-12-31"), 7200)
        ));

        List<GetStudyTimeGrassUseCase.StudyTimeGrassView> result =
                leapYearService.handle(new GetStudyTimeGrassQuery(1L));

        assertThat(result).hasSize(366);
        assertThat(result.get(0).date()).isEqualTo(LocalDate.parse("2024-01-01"));
        assertThat(result.get(365).date()).isEqualTo(LocalDate.parse("2024-12-31"));
        assertThat(result.get(365).studySeconds()).isEqualTo(7200);
        assertThat(result.get(365).level()).isEqualTo(4);
        assertThat(result.get(365).isFuture()).isFalse();
    }

    @Test
    void sumsDuplicatedDateRowsDefensively() {
        when(repository.findByMemberIdAndDateBetween(
                1L,
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-01-03")
        )).thenReturn(List.of(
                new StudyTimeGrassStat(1L, LocalDate.parse("2026-01-02"), 1000),
                new StudyTimeGrassStat(1L, LocalDate.parse("2026-01-02"), 900)
        ));

        List<GetStudyTimeGrassUseCase.StudyTimeGrassView> result =
                service.handle(new GetStudyTimeGrassQuery(1L));

        assertThat(result.get(1).studySeconds()).isEqualTo(1900);
        assertThat(result.get(1).level()).isEqualTo(2);
        assertThat(result.get(1).isFuture()).isFalse();
    }

    @Test
    void rejectsNullMemberId() {
        assertThatThrownBy(() -> service.handle(new GetStudyTimeGrassQuery(null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 ID는 필수입니다.");
    }
}
