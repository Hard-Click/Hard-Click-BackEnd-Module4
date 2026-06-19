package com.wanted.backend.domain.stats.application.service;

import com.wanted.backend.domain.stats.application.query.GetDailyStudyStatQuery;
import com.wanted.backend.domain.stats.application.usecase.GetDailyStudyStatUseCase;
import com.wanted.backend.domain.stats.domain.model.DailyStudyStat;
import com.wanted.backend.domain.stats.domain.repository.DailyStudyStatsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class GetDailyStudyStatServiceTest {

    private DailyStudyStatsRepository repository;
    private GetDailyStudyStatService service;

    @BeforeEach
    void setUp() {
        repository = mock(DailyStudyStatsRepository.class);
        service = new GetDailyStudyStatService(repository);
    }

    @Test
    void returnsDailyStudyStatWhenDataExists() {
        LocalDate date = LocalDate.parse("2026-06-18");
        when(repository.findByMemberIdAndStatDate(1L, date))
                .thenReturn(Optional.of(new DailyStudyStat(
                        1L,
                        date,
                        3,
                        9000,
                        2
                )));

        GetDailyStudyStatUseCase.DailyStudyStatView result =
                service.handle(new GetDailyStudyStatQuery(1L, date));

        assertThat(result.date()).isEqualTo(date);
        assertThat(result.watchedLessonCount()).isEqualTo(3);
        assertThat(result.studySeconds()).isEqualTo(9000);
        assertThat(result.completedLessonCount()).isEqualTo(2);
        verify(repository).findByMemberIdAndStatDate(1L, date);
    }

    @Test
    void returnsZeroValuesWhenDataDoesNotExist() {
        LocalDate date = LocalDate.parse("2026-06-18");
        when(repository.findByMemberIdAndStatDate(1L, date)).thenReturn(Optional.empty());

        GetDailyStudyStatUseCase.DailyStudyStatView result =
                service.handle(new GetDailyStudyStatQuery(1L, date));

        assertThat(result.date()).isEqualTo(date);
        assertThat(result.watchedLessonCount()).isZero();
        assertThat(result.studySeconds()).isZero();
        assertThat(result.completedLessonCount()).isZero();
        verify(repository).findByMemberIdAndStatDate(1L, date);
    }

    @Test
    void rejectsNullMemberId() {
        LocalDate date = LocalDate.parse("2026-06-18");

        assertThatThrownBy(() -> service.handle(new GetDailyStudyStatQuery(null, date)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 ID는 필수입니다.");

        verifyNoInteractions(repository);
    }

    @Test
    void rejectsNullDate() {
        assertThatThrownBy(() -> service.handle(new GetDailyStudyStatQuery(1L, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("조회 날짜는 필수입니다.");

        verifyNoInteractions(repository);
    }
}
