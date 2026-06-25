package com.wanted.backend.domain.grass.infrastructure.cache;

import com.wanted.backend.domain.grass.application.query.GetMonthlyGrassQuery;
import com.wanted.backend.domain.grass.application.usecase.GetMonthlyGrassUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.cache.annotation.Cacheable;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CachedGetMonthlyGrassUseCaseTest {

    @Test
    void delegatesMonthlyGrassQuery() {
        GetMonthlyGrassUseCase delegate = mock(GetMonthlyGrassUseCase.class);
        CachedGetMonthlyGrassUseCase cachedUseCase = new CachedGetMonthlyGrassUseCase(delegate);
        GetMonthlyGrassQuery query = new GetMonthlyGrassQuery(1L, 2026, 6);
        GetMonthlyGrassUseCase.MonthlyGrassView expected =
                new GetMonthlyGrassUseCase.MonthlyGrassView(2026, 6, List.of());
        when(delegate.handle(query)).thenReturn(expected);

        GetMonthlyGrassUseCase.MonthlyGrassView result = cachedUseCase.handle(query);

        assertThat(result).isSameAs(expected);
        verify(delegate).handle(query);
    }

    @Test
    void handleMethodHasCacheableAnnotation() throws NoSuchMethodException {
        Method handleMethod = CachedGetMonthlyGrassUseCase.class.getMethod(
                "handle",
                GetMonthlyGrassQuery.class
        );

        Cacheable cacheable = handleMethod.getAnnotation(Cacheable.class);

        assertThat(cacheable).isNotNull();
        assertThat(cacheable.cacheNames()).containsExactly("grassMonthly:v2");
        assertThat(cacheable.key())
                .contains("#query.memberId()")
                .contains("#query.year()")
                .contains("#query.month()")
                .contains("LocalDate")
                .contains("@clock");
    }
}
