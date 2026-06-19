package com.wanted.backend.domain.grass.application.service;

import com.wanted.backend.domain.grass.application.mapper.YearlyGrassViewMapper;
import com.wanted.backend.domain.grass.application.query.GetYearlyGrassQuery;
import com.wanted.backend.domain.grass.application.usecase.GetYearlyGrassUseCase;
import com.wanted.backend.domain.grass.application.usecase.GetYearlyGrassUseCase.YearlyGrassDayView;
import com.wanted.backend.domain.grass.domain.model.YearlyGrassStat;
import com.wanted.backend.domain.grass.domain.policy.LessonGrassLevelPolicy;
import com.wanted.backend.domain.grass.domain.policy.YearlyGrassPeriodPolicy;
import com.wanted.backend.domain.grass.domain.repository.YearlyGrassRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class GetYearlyGrassServiceTest {

    private YearlyGrassRepository repository;
    private GetYearlyGrassService service;

    @BeforeEach
    void setUp() {
        repository = mock(YearlyGrassRepository.class);
        Clock clock = Clock.fixed(
                Instant.parse("2026-01-03T00:00:00Z"),
                ZoneId.of("Asia/Seoul")
        );
        service = new GetYearlyGrassService(
                repository,
                new YearlyGrassViewMapper(new LessonGrassLevelPolicy(4)),
                new YearlyGrassPeriodPolicy(),
                clock
        );
    }

    @Test
    void returnsYearlyGrassWithZeroFilledFutureDates() {
        LocalDate startDate = LocalDate.parse("2026-01-01");
        LocalDate today = LocalDate.parse("2026-01-03");
        when(repository.findByMemberIdAndDateBetween(1L, startDate, today))
                .thenReturn(List.of(
                        new YearlyGrassStat(1L, LocalDate.parse("2026-01-01"), 1),
                        new YearlyGrassStat(1L, LocalDate.parse("2026-01-03"), 5)
                ));

        GetYearlyGrassUseCase.YearlyGrassView result =
                service.handle(new GetYearlyGrassQuery(1L, 2026));

        assertThat(result.year()).isEqualTo(2026);
        assertThat(result.days()).hasSize(365);
        assertThat(result.days().subList(0, 4))
                .extracting(
                        YearlyGrassDayView::date,
                        YearlyGrassDayView::value,
                        YearlyGrassDayView::level,
                        YearlyGrassDayView::isFuture
                )
                .containsExactly(
                        tuple(LocalDate.parse("2026-01-01"), 1, 1, false),
                        tuple(LocalDate.parse("2026-01-02"), 0, 0, false),
                        tuple(LocalDate.parse("2026-01-03"), 5, 4, false),
                        tuple(LocalDate.parse("2026-01-04"), 0, 0, true)
                );
        assertThat(result.days().get(364).date()).isEqualTo(LocalDate.parse("2026-12-31"));
        assertThat(result.days().get(364).isFuture()).isTrue();
    }

    @Test
    void sumsDuplicatedDateRowsDefensively() {
        LocalDate startDate = LocalDate.parse("2026-01-01");
        LocalDate today = LocalDate.parse("2026-01-03");
        when(repository.findByMemberIdAndDateBetween(1L, startDate, today))
                .thenReturn(List.of(
                        new YearlyGrassStat(1L, LocalDate.parse("2026-01-02"), 1),
                        new YearlyGrassStat(1L, LocalDate.parse("2026-01-02"), 2)
                ));

        GetYearlyGrassUseCase.YearlyGrassView result =
                service.handle(new GetYearlyGrassQuery(1L, 2026));

        assertThat(result.days().get(1).value()).isEqualTo(3);
        assertThat(result.days().get(1).level()).isEqualTo(3);
        assertThat(result.days().get(1).isFuture()).isFalse();
    }

    @Test
    void rejectsNullMemberId() {
        assertThatThrownBy(() -> service.handle(new GetYearlyGrassQuery(null, 2026)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 ID는 필수입니다.");
    }

    @Test
    void returnsRequestedPastYearGrass() {
        LocalDate startDate = LocalDate.parse("2025-01-01");
        LocalDate endDate = LocalDate.parse("2025-12-31");
        when(repository.findByMemberIdAndDateBetween(1L, startDate, endDate))
                .thenReturn(List.of(
                        new YearlyGrassStat(1L, LocalDate.parse("2025-12-31"), 2)
                ));

        GetYearlyGrassUseCase.YearlyGrassView result =
                service.handle(new GetYearlyGrassQuery(1L, 2025));

        assertThat(result.year()).isEqualTo(2025);
        assertThat(result.days()).hasSize(365);
        assertThat(result.days().get(364).date()).isEqualTo(LocalDate.parse("2025-12-31"));
        assertThat(result.days().get(364).value()).isEqualTo(2);
        assertThat(result.days().get(364).level()).isEqualTo(2);
        assertThat(result.days().get(364).isFuture()).isFalse();
    }

    @Test
    void returnsFutureYearWithoutRepositoryQuery() {
        GetYearlyGrassUseCase.YearlyGrassView result =
                service.handle(new GetYearlyGrassQuery(1L, 2027));

        assertThat(result.year()).isEqualTo(2027);
        assertThat(result.days()).hasSize(365);
        assertThat(result.days().get(0).date()).isEqualTo(LocalDate.parse("2027-01-01"));
        assertThat(result.days().get(0).value()).isZero();
        assertThat(result.days().get(0).level()).isZero();
        assertThat(result.days().get(0).isFuture()).isTrue();
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsNullYear() {
        assertThatThrownBy(() -> service.handle(new GetYearlyGrassQuery(1L, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("조회 연도는 필수입니다.");
    }
}
