package com.wanted.backend.domain.grass.application.service;

import com.wanted.backend.domain.grass.application.mapper.MonthlyGrassViewMapper;
import com.wanted.backend.domain.grass.application.query.GetMonthlyGrassQuery;
import com.wanted.backend.domain.grass.application.usecase.GetMonthlyGrassUseCase;
import com.wanted.backend.domain.grass.application.usecase.GetMonthlyGrassUseCase.MonthlyGrassDayView;
import com.wanted.backend.domain.grass.domain.model.MonthlyGrassStat;
import com.wanted.backend.domain.grass.domain.policy.LessonGrassLevelPolicy;
import com.wanted.backend.domain.grass.domain.policy.MonthlyGrassPeriodPolicy;
import com.wanted.backend.domain.grass.domain.repository.MonthlyGrassRepository;
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

class GetMonthlyGrassServiceTest {

    private MonthlyGrassRepository repository;
    private GetMonthlyGrassService service;

    @BeforeEach
    void setUp() {
        repository = mock(MonthlyGrassRepository.class);
        Clock clock = Clock.fixed(
                Instant.parse("2026-06-03T00:00:00Z"),
                ZoneId.of("Asia/Seoul")
        );
        service = new GetMonthlyGrassService(
                repository,
                new MonthlyGrassViewMapper(new LessonGrassLevelPolicy(4)),
                new MonthlyGrassPeriodPolicy(),
                clock
        );
    }

    @Test
    void returnsMonthlyGrassWithZeroFilledFutureDates() {
        LocalDate startDate = LocalDate.parse("2026-06-01");
        LocalDate today = LocalDate.parse("2026-06-03");
        when(repository.findByMemberIdAndDateBetween(1L, startDate, today))
                .thenReturn(List.of(
                        new MonthlyGrassStat(1L, LocalDate.parse("2026-06-01"), 1),
                        new MonthlyGrassStat(1L, LocalDate.parse("2026-06-03"), 5)
                ));

        GetMonthlyGrassUseCase.MonthlyGrassView result =
                service.handle(new GetMonthlyGrassQuery(1L, 2026, 6));

        assertThat(result.year()).isEqualTo(2026);
        assertThat(result.month()).isEqualTo(6);
        assertThat(result.days()).hasSize(30);
        assertThat(result.days().subList(0, 4))
                .extracting(
                        MonthlyGrassDayView::date,
                        MonthlyGrassDayView::value,
                        MonthlyGrassDayView::level,
                        MonthlyGrassDayView::isFuture
                )
                .containsExactly(
                        tuple(LocalDate.parse("2026-06-01"), 1, 1, false),
                        tuple(LocalDate.parse("2026-06-02"), 0, 0, false),
                        tuple(LocalDate.parse("2026-06-03"), 5, 4, false),
                        tuple(LocalDate.parse("2026-06-04"), 0, 0, true)
                );
        assertThat(result.days().get(29).date()).isEqualTo(LocalDate.parse("2026-06-30"));
        assertThat(result.days().get(29).isFuture()).isTrue();
    }

    @Test
    void returnsLeapFebruaryWith29Days() {
        Clock leapYearClock = Clock.fixed(
                Instant.parse("2028-02-03T00:00:00Z"),
                ZoneId.of("Asia/Seoul")
        );
        GetMonthlyGrassService leapYearService = new GetMonthlyGrassService(
                repository,
                new MonthlyGrassViewMapper(new LessonGrassLevelPolicy(4)),
                new MonthlyGrassPeriodPolicy(),
                leapYearClock
        );
        LocalDate startDate = LocalDate.parse("2028-02-01");
        LocalDate today = LocalDate.parse("2028-02-03");
        when(repository.findByMemberIdAndDateBetween(1L, startDate, today))
                .thenReturn(List.of());

        GetMonthlyGrassUseCase.MonthlyGrassView result =
                leapYearService.handle(new GetMonthlyGrassQuery(1L, 2028, 2));

        assertThat(result.days()).hasSize(29);
        assertThat(result.days().get(28).date()).isEqualTo(LocalDate.parse("2028-02-29"));
    }

    @Test
    void returnsPastMonthGrass() {
        LocalDate startDate = LocalDate.parse("2026-05-01");
        LocalDate endDate = LocalDate.parse("2026-05-31");
        when(repository.findByMemberIdAndDateBetween(1L, startDate, endDate))
                .thenReturn(List.of(
                        new MonthlyGrassStat(1L, LocalDate.parse("2026-05-31"), 2)
                ));

        GetMonthlyGrassUseCase.MonthlyGrassView result =
                service.handle(new GetMonthlyGrassQuery(1L, 2026, 5));

        assertThat(result.days()).hasSize(31);
        assertThat(result.days().get(30).date()).isEqualTo(LocalDate.parse("2026-05-31"));
        assertThat(result.days().get(30).value()).isEqualTo(2);
        assertThat(result.days().get(30).level()).isEqualTo(2);
        assertThat(result.days().get(30).isFuture()).isFalse();
    }

    @Test
    void returnsFutureMonthWithoutRepositoryQuery() {
        GetMonthlyGrassUseCase.MonthlyGrassView result =
                service.handle(new GetMonthlyGrassQuery(1L, 2026, 7));

        assertThat(result.year()).isEqualTo(2026);
        assertThat(result.month()).isEqualTo(7);
        assertThat(result.days()).hasSize(31);
        assertThat(result.days().get(0).date()).isEqualTo(LocalDate.parse("2026-07-01"));
        assertThat(result.days().get(0).value()).isZero();
        assertThat(result.days().get(0).level()).isZero();
        assertThat(result.days().get(0).isFuture()).isTrue();
        verifyNoInteractions(repository);
    }

    @Test
    void returnsZeroLevelWhenMonthHasNoData() {
        LocalDate startDate = LocalDate.parse("2026-06-01");
        LocalDate today = LocalDate.parse("2026-06-03");
        when(repository.findByMemberIdAndDateBetween(1L, startDate, today))
                .thenReturn(List.of());

        GetMonthlyGrassUseCase.MonthlyGrassView result =
                service.handle(new GetMonthlyGrassQuery(1L, 2026, 6));

        assertThat(result.days().get(0).value()).isZero();
        assertThat(result.days().get(0).level()).isZero();
    }

    @Test
    void sumsDuplicatedDateRowsDefensively() {
        LocalDate startDate = LocalDate.parse("2026-06-01");
        LocalDate today = LocalDate.parse("2026-06-03");
        when(repository.findByMemberIdAndDateBetween(1L, startDate, today))
                .thenReturn(List.of(
                        new MonthlyGrassStat(1L, LocalDate.parse("2026-06-02"), 1),
                        new MonthlyGrassStat(1L, LocalDate.parse("2026-06-02"), 2)
                ));

        GetMonthlyGrassUseCase.MonthlyGrassView result =
                service.handle(new GetMonthlyGrassQuery(1L, 2026, 6));

        assertThat(result.days().get(1).value()).isEqualTo(3);
        assertThat(result.days().get(1).level()).isEqualTo(3);
        assertThat(result.days().get(1).isFuture()).isFalse();
    }

    @Test
    void rejectsMonthOutOfRange() {
        assertThatThrownBy(() -> service.handle(new GetMonthlyGrassQuery(1L, 2026, 13)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("조회 월은 1~12 사이여야 합니다.");
    }
}
