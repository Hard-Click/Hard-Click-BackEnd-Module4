package com.wanted.backend.domain.grass.infrastructure.cache;

import com.wanted.backend.domain.grass.application.query.GetGrassViewQuery;
import com.wanted.backend.domain.grass.application.usecase.GetGrassViewUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.cache.annotation.Cacheable;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CachedGetGrassViewUseCaseTest {

    @Test
    void delegatesGrassViewQuery() {
        GetGrassViewUseCase delegate = mock(GetGrassViewUseCase.class);
        CachedGetGrassViewUseCase cachedUseCase = new CachedGetGrassViewUseCase(delegate);
        GetGrassViewQuery query = new GetGrassViewQuery(1L, "monthly", 2026, 6);
        GetGrassViewUseCase.GrassView expected =
                new GetGrassViewUseCase.GrassView("monthly", 2026, 6, List.of());
        when(delegate.handle(query)).thenReturn(expected);

        GetGrassViewUseCase.GrassView result = cachedUseCase.handle(query);

        assertThat(result).isSameAs(expected);
        verify(delegate).handle(query);
    }

    @Test
    void handleMethodHasCacheableAnnotation() throws NoSuchMethodException {
        Method handleMethod = CachedGetGrassViewUseCase.class.getMethod(
                "handle",
                GetGrassViewQuery.class
        );

        Cacheable cacheable = handleMethod.getAnnotation(Cacheable.class);

        assertThat(cacheable).isNotNull();
        assertThat(cacheable.cacheNames()).containsExactly("grassView");
        assertThat(cacheable.key())
                .contains("#query.memberId()")
                .contains("#query.view()")
                .contains("#query.year()")
                .contains("#query.month()")
                .contains("LocalDate")
                .contains("@clock");
    }
}
