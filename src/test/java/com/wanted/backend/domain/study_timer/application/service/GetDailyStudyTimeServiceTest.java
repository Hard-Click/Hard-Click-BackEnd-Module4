package com.wanted.backend.domain.study_timer.application.service;

import com.wanted.backend.domain.study_timer.application.query.GetDailyStudyTimeQuery;
import com.wanted.backend.domain.study_timer.application.usecase.GetDailyStudyTimeUseCase;
import com.wanted.backend.domain.study_timer.domain.model.DailyStudyStat;
import com.wanted.backend.domain.study_timer.domain.repository.DailyStudyStatsRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class GetDailyStudyTimeServiceTest {

    private DailyStudyStatsRepository repository;
    private GetDailyStudyTimeService service;

    @BeforeEach
    void setUp() {
        repository = mock(DailyStudyStatsRepository.class);
        service = new GetDailyStudyTimeService(repository);
    }

    @Test
    void returnsDailyStudyTimesWithZeroFilledDates() {
        LocalDate startDate = LocalDate.parse("2026-05-01");
        LocalDate endDate = LocalDate.parse("2026-05-03");
        when(repository.findByMemberIdAndDateBetween(1L, startDate, endDate))
                .thenReturn(List.of(
                        new DailyStudyStat(1L, LocalDate.parse("2026-05-01"), 120),
                        new DailyStudyStat(1L, LocalDate.parse("2026-05-03"), 300)
                ));

        List<GetDailyStudyTimeUseCase.DailyStudyTimeItem> result =
                service.handle(new GetDailyStudyTimeQuery(1L, startDate, endDate));

        assertThat(result)
                .extracting(
                        GetDailyStudyTimeUseCase.DailyStudyTimeItem::date,
                        GetDailyStudyTimeUseCase.DailyStudyTimeItem::studySeconds
                )
                .containsExactly(
                        tuple(LocalDate.parse("2026-05-01"), 120),
                        tuple(LocalDate.parse("2026-05-02"), 0),
                        tuple(LocalDate.parse("2026-05-03"), 300)
                );

        verify(repository).findByMemberIdAndDateBetween(1L, startDate, endDate);
    }

    @Test
    void sumsDuplicatedDateRowsDefensively() {
        LocalDate startDate = LocalDate.parse("2026-05-01");
        LocalDate endDate = LocalDate.parse("2026-05-01");
        when(repository.findByMemberIdAndDateBetween(1L, startDate, endDate))
                .thenReturn(List.of(
                        new DailyStudyStat(1L, LocalDate.parse("2026-05-01"), 120),
                        new DailyStudyStat(1L, LocalDate.parse("2026-05-01"), 80)
                ));

        List<GetDailyStudyTimeUseCase.DailyStudyTimeItem> result =
                service.handle(new GetDailyStudyTimeQuery(1L, startDate, endDate));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).studySeconds()).isEqualTo(200);
    }

    @Test
    void rejectsDateRangeWhenStartDateIsAfterEndDate() {
        LocalDate startDate = LocalDate.parse("2026-05-03");
        LocalDate endDate = LocalDate.parse("2026-05-01");

        assertThatThrownBy(() -> service.handle(new GetDailyStudyTimeQuery(1L, startDate, endDate)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_DAILY_DATE_RANGE_INVALID);

        verify(repository, never()).findByMemberIdAndDateBetween(1L, startDate, endDate);
    }

    @Test
    void rejectsQueryWhenMemberIdIsNull() {
        LocalDate startDate = LocalDate.parse("2026-05-01");
        LocalDate endDate = LocalDate.parse("2026-05-03");

        assertThatThrownBy(() -> service.handle(new GetDailyStudyTimeQuery(null, startDate, endDate)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_MEMBER_ID_REQUIRED);

        verifyNoInteractions(repository);
    }

    @Test
    void rejectsQueryWhenStartDateIsNull() {
        LocalDate endDate = LocalDate.parse("2026-05-03");

        assertThatThrownBy(() -> service.handle(new GetDailyStudyTimeQuery(1L, null, endDate)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_DAILY_START_DATE_REQUIRED);

        verifyNoInteractions(repository);
    }

    @Test
    void rejectsQueryWhenEndDateIsNull() {
        LocalDate startDate = LocalDate.parse("2026-05-01");

        assertThatThrownBy(() -> service.handle(new GetDailyStudyTimeQuery(1L, startDate, null)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_DAILY_END_DATE_REQUIRED);

        verifyNoInteractions(repository);
    }

    @Test
    void rejectsDateRangeLongerThanOneYear() {
        LocalDate startDate = LocalDate.parse("2026-01-01");
        LocalDate endDate = LocalDate.parse("2027-01-01");

        assertThatThrownBy(() -> service.handle(new GetDailyStudyTimeQuery(1L, startDate, endDate)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_DAILY_DATE_RANGE_TOO_LONG);

        verifyNoInteractions(repository);
    }
}
