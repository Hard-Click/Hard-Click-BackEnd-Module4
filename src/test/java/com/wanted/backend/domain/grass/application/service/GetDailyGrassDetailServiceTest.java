package com.wanted.backend.domain.grass.application.service;

import com.wanted.backend.domain.grass.application.query.GetDailyGrassDetailQuery;
import com.wanted.backend.domain.grass.application.usecase.GetDailyGrassDetailUseCase.DailyGrassDetailView;
import com.wanted.backend.domain.grass.domain.model.DailyGrassDetailStat;
import com.wanted.backend.domain.grass.domain.policy.GrassLearningStatusPolicy;
import com.wanted.backend.domain.grass.domain.repository.DailyGrassDetailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetDailyGrassDetailServiceTest {

    private DailyGrassDetailRepository repository;
    private GetDailyGrassDetailService service;

    @BeforeEach
    void setUp() {
        repository = mock(DailyGrassDetailRepository.class);
        service = new GetDailyGrassDetailService(
                repository,
                new GrassLearningStatusPolicy()
        );
    }

    @Test
    void returnsDailyGrassDetail() {
        LocalDate date = LocalDate.parse("2026-06-18");
        when(repository.findByMemberIdAndStatDate(1L, date))
                .thenReturn(Optional.of(
                        new DailyGrassDetailStat(1L, date, 3, 5400)
                ));

        DailyGrassDetailView result = service.handle(new GetDailyGrassDetailQuery(1L, date));

        assertThat(result.date()).isEqualTo(date);
        assertThat(result.watchedLessonCount()).isEqualTo(3);
        assertThat(result.studySeconds()).isEqualTo(5400);
        assertThat(result.hasStudyRecord()).isTrue();
    }

    @Test
    void returnsDefaultValuesWhenDailyStatDoesNotExist() {
        LocalDate date = LocalDate.parse("2026-06-18");
        when(repository.findByMemberIdAndStatDate(1L, date))
                .thenReturn(Optional.empty());

        DailyGrassDetailView result = service.handle(new GetDailyGrassDetailQuery(1L, date));

        assertThat(result.date()).isEqualTo(date);
        assertThat(result.watchedLessonCount()).isZero();
        assertThat(result.studySeconds()).isZero();
        assertThat(result.hasStudyRecord()).isFalse();
    }
}
