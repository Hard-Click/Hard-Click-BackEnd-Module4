package com.wanted.backend.domain.grass.application.service;

import com.wanted.backend.domain.grass.application.query.GetLessonGrassQuery;
import com.wanted.backend.domain.grass.application.usecase.GetLessonGrassUseCase;
import com.wanted.backend.domain.grass.domain.model.LessonGrassStat;
import com.wanted.backend.domain.grass.domain.policy.LessonGrassLevelPolicy;
import com.wanted.backend.domain.grass.domain.repository.LessonGrassRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetLessonGrassServiceTest {

    private LessonGrassRepository repository;
    private GetLessonGrassService service;

    @BeforeEach
    void setUp() {
        repository = mock(LessonGrassRepository.class);
        Clock clock = Clock.fixed(
                Instant.parse("2026-01-03T00:00:00Z"),
                ZoneId.of("Asia/Seoul")
        );
        service = new GetLessonGrassService(repository, new LessonGrassLevelPolicy(4), clock);
    }

    @Test
    void returnsYearlyLessonGrassWithZeroFilledFutureDates() {
        LocalDate startDate = LocalDate.parse("2026-01-01");
        LocalDate today = LocalDate.parse("2026-01-03");
        when(repository.findByMemberIdAndDateBetween(1L, startDate, today))
                .thenReturn(List.of(
                        new LessonGrassStat(1L, LocalDate.parse("2026-01-01"), 1),
                        new LessonGrassStat(1L, LocalDate.parse("2026-01-03"), 5)
                ));

        List<GetLessonGrassUseCase.LessonGrassView> result =
                service.handle(new GetLessonGrassQuery(1L));

        assertThat(result).hasSize(365);
        assertThat(result.subList(0, 4))
                .extracting(
                        GetLessonGrassUseCase.LessonGrassView::date,
                        GetLessonGrassUseCase.LessonGrassView::watchedLessonCount,
                        GetLessonGrassUseCase.LessonGrassView::level,
                        GetLessonGrassUseCase.LessonGrassView::isFuture
                )
                .containsExactly(
                        tuple(LocalDate.parse("2026-01-01"), 1, 1, false),
                        tuple(LocalDate.parse("2026-01-02"), 0, 0, false),
                        tuple(LocalDate.parse("2026-01-03"), 5, 4, false),
                        tuple(LocalDate.parse("2026-01-04"), 0, 0, true)
                );
        assertThat(result.get(364).date()).isEqualTo(LocalDate.parse("2026-12-31"));
        assertThat(result.get(364).isFuture()).isTrue();

        verify(repository).findByMemberIdAndDateBetween(1L, startDate, today);
    }

    @Test
    void sumsDuplicatedDateRowsDefensively() {
        LocalDate startDate = LocalDate.parse("2026-01-01");
        LocalDate today = LocalDate.parse("2026-01-03");
        when(repository.findByMemberIdAndDateBetween(1L, startDate, today))
                .thenReturn(List.of(
                        new LessonGrassStat(1L, LocalDate.parse("2026-01-02"), 1),
                        new LessonGrassStat(1L, LocalDate.parse("2026-01-02"), 2)
                ));

        List<GetLessonGrassUseCase.LessonGrassView> result =
                service.handle(new GetLessonGrassQuery(1L));

        assertThat(result.get(1).watchedLessonCount()).isEqualTo(3);
        assertThat(result.get(1).level()).isEqualTo(3);
        assertThat(result.get(1).isFuture()).isFalse();
    }

    @Test
    void rejectsNullMemberId() {
        assertThatThrownBy(() -> service.handle(new GetLessonGrassQuery(null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 ID는 필수입니다.");

        verify(repository, never()).findByMemberIdAndDateBetween(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }
}
