package com.wanted.backend.domain.grass.application.service;

import com.wanted.backend.domain.grass.application.query.GetGrassViewQuery;
import com.wanted.backend.domain.grass.application.query.GetMonthlyGrassQuery;
import com.wanted.backend.domain.grass.application.query.GetYearlyGrassQuery;
import com.wanted.backend.domain.grass.application.usecase.GetGrassViewUseCase.GrassView;
import com.wanted.backend.domain.grass.application.usecase.GetMonthlyGrassUseCase;
import com.wanted.backend.domain.grass.application.usecase.GetYearlyGrassUseCase;
import com.wanted.backend.domain.grass.domain.policy.GrassViewModePolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetGrassViewServiceTest {

    private GetMonthlyGrassUseCase getMonthlyGrassUseCase;
    private GetYearlyGrassUseCase getYearlyGrassUseCase;
    private GetGrassViewService service;

    @BeforeEach
    void setUp() {
        getMonthlyGrassUseCase = mock(GetMonthlyGrassUseCase.class);
        getYearlyGrassUseCase = mock(GetYearlyGrassUseCase.class);
        service = new GetGrassViewService(
                getMonthlyGrassUseCase,
                getYearlyGrassUseCase,
                new GrassViewModePolicy()
        );
    }

    @Test
    void returnsMonthlyGrassView() {
        when(getMonthlyGrassUseCase.handle(any(GetMonthlyGrassQuery.class)))
                .thenReturn(new GetMonthlyGrassUseCase.MonthlyGrassView(
                        2026,
                        6,
                        List.of(new GetMonthlyGrassUseCase.MonthlyGrassDayView(
                                LocalDate.parse("2026-06-01"),
                                2,
                                2,
                                false
                        ))
                ));

        GrassView result = service.handle(new GetGrassViewQuery(1L, "monthly", 2026, 6));

        assertThat(result.view()).isEqualTo("monthly");
        assertThat(result.year()).isEqualTo(2026);
        assertThat(result.month()).isEqualTo(6);
        assertThat(result.days()).hasSize(1);
        assertThat(result.days().get(0).date()).isEqualTo(LocalDate.parse("2026-06-01"));
        assertThat(result.days().get(0).value()).isEqualTo(2);
        verify(getMonthlyGrassUseCase).handle(argThat(query ->
                query.memberId().equals(1L)
                        && query.year().equals(2026)
                        && query.month().equals(6)
        ));
        verify(getYearlyGrassUseCase, never()).handle(any());
    }

    @Test
    void returnsYearlyGrassView() {
        when(getYearlyGrassUseCase.handle(any(GetYearlyGrassQuery.class)))
                .thenReturn(new GetYearlyGrassUseCase.YearlyGrassView(
                        2026,
                        List.of(new GetYearlyGrassUseCase.YearlyGrassDayView(
                                LocalDate.parse("2026-01-01"),
                                1,
                                1,
                                false
                        ))
                ));

        GrassView result = service.handle(new GetGrassViewQuery(1L, "yearly", 2026, null));

        assertThat(result.view()).isEqualTo("yearly");
        assertThat(result.year()).isEqualTo(2026);
        assertThat(result.month()).isNull();
        assertThat(result.days()).hasSize(1);
        assertThat(result.days().get(0).date()).isEqualTo(LocalDate.parse("2026-01-01"));
        assertThat(result.days().get(0).value()).isEqualTo(1);
        verify(getYearlyGrassUseCase).handle(argThat(query ->
                query.memberId().equals(1L)
                        && query.year().equals(2026)
        ));
        verify(getMonthlyGrassUseCase, never()).handle(any());
    }

    @Test
    void rejectsInvalidViewMode() {
        assertThatThrownBy(() -> service.handle(new GetGrassViewQuery(1L, "weekly", 2026, 6)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("잔디 보기 모드는 monthly 또는 yearly여야 합니다.");
    }

    @Test
    void rejectsMonthlyViewWithoutMonth() {
        assertThatThrownBy(() -> service.handle(new GetGrassViewQuery(1L, "monthly", 2026, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("월별 잔디 조회 시 month는 필수입니다.");

        verify(getMonthlyGrassUseCase, never()).handle(any());
        verify(getYearlyGrassUseCase, never()).handle(any());
    }
}
