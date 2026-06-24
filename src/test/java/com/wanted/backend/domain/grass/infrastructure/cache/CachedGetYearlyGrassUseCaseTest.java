package com.wanted.backend.domain.grass.infrastructure.cache;

import com.wanted.backend.domain.grass.application.query.GetYearlyGrassQuery;
import com.wanted.backend.domain.grass.application.usecase.GetYearlyGrassUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.cache.annotation.Cacheable;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CachedGetYearlyGrassUseCaseTest {

    @Test
    void delegatesYearlyGrassQuery() {
        GetYearlyGrassUseCase delegate = mock(GetYearlyGrassUseCase.class);
        CachedGetYearlyGrassUseCase cachedUseCase = new CachedGetYearlyGrassUseCase(delegate);
        GetYearlyGrassQuery query = new GetYearlyGrassQuery(1L, 2026);
        GetYearlyGrassUseCase.YearlyGrassView expected =
                new GetYearlyGrassUseCase.YearlyGrassView(2026, List.of());
        when(delegate.handle(query)).thenReturn(expected);

        GetYearlyGrassUseCase.YearlyGrassView result = cachedUseCase.handle(query);

        assertThat(result).isSameAs(expected);
        verify(delegate).handle(query);
    }

    @Test
    void handleMethodHasCacheableAnnotation() throws NoSuchMethodException {
        Method handleMethod = CachedGetYearlyGrassUseCase.class.getMethod(
                "handle",
                GetYearlyGrassQuery.class
        );

        Cacheable cacheable = handleMethod.getAnnotation(Cacheable.class);

        assertThat(cacheable).isNotNull();
        assertThat(cacheable.cacheNames()).containsExactly("grassYearlyV2");
        assertThat(cacheable.key())
                .contains("#query.memberId()")
                .contains("#query.year()")
                .contains("LocalDate")
                .contains("@clock");
    }
}
