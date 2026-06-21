package com.wanted.backend.domain.grass.infrastructure.cache;

import com.wanted.backend.domain.grass.application.query.GetYearlyGrassQuery;
import com.wanted.backend.domain.grass.application.usecase.GetYearlyGrassUseCase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class CachedGetYearlyGrassUseCase implements GetYearlyGrassUseCase {

    private final GetYearlyGrassUseCase delegate;

    public CachedGetYearlyGrassUseCase(
            @Qualifier("getYearlyGrassService") GetYearlyGrassUseCase delegate
    ) {
        this.delegate = delegate;
    }

    @Override
    @Cacheable(cacheNames = "grassYearly", key = "#query.memberId() + ':' + #query.year() + ':' + T(java.time.LocalDate).now(@clock)")
    public YearlyGrassView handle(GetYearlyGrassQuery query) {
        return delegate.handle(query);
    }
}
