package com.wanted.backend.domain.grass.infrastructure.cache;

import com.wanted.backend.domain.grass.application.query.GetGrassViewQuery;
import com.wanted.backend.domain.grass.application.usecase.GetGrassViewUseCase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class CachedGetGrassViewUseCase implements GetGrassViewUseCase {

    private final GetGrassViewUseCase delegate;

    public CachedGetGrassViewUseCase(
            @Qualifier("getGrassViewService") GetGrassViewUseCase delegate
    ) {
        this.delegate = delegate;
    }

    @Override
    @Cacheable(cacheNames = "grassView", key = "#query.memberId() + ':' + (#query.view() == null ? null : #query.view().trim().toLowerCase()) + ':' + #query.year() + ':' + #query.month() + ':' + T(java.time.LocalDate).now(@clock)")
    public GrassView handle(GetGrassViewQuery query) {
        return delegate.handle(query);
    }
}
