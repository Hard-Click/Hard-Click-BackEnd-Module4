package com.wanted.backend.domain.grass.infrastructure.cache;

import com.wanted.backend.domain.grass.application.query.GetMonthlyGrassQuery;
import com.wanted.backend.domain.grass.application.usecase.GetMonthlyGrassUseCase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class CachedGetMonthlyGrassUseCase implements GetMonthlyGrassUseCase {

    private final GetMonthlyGrassUseCase delegate;

    public CachedGetMonthlyGrassUseCase(
            @Qualifier("getMonthlyGrassService") GetMonthlyGrassUseCase delegate
    ) {
        this.delegate = delegate;
    }

    @Override
    @Cacheable(cacheNames = "grassMonthlyV2", key = "#query.memberId() + ':' + #query.year() + ':' + #query.month() + ':' + T(java.time.LocalDate).now(@clock)")
    public MonthlyGrassView handle(GetMonthlyGrassQuery query) {
        return delegate.handle(query);
    }
}
