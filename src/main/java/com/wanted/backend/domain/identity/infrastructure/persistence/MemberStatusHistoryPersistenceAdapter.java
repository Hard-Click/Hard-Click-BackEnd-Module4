package com.wanted.backend.domain.identity.infrastructure.persistence;

import com.wanted.backend.domain.identity.domain.model.MemberStatusHistory;
import com.wanted.backend.domain.identity.domain.repository.MemberStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberStatusHistoryPersistenceAdapter implements MemberStatusHistoryRepository {

    private final MemberStatusHistoryJpaRepository memberStatusHistoryJpaRepository;

    @Override
    public MemberStatusHistory save(MemberStatusHistory history) {
        MemberStatusHistoryJpaEntity entity = MemberStatusHistoryJpaEntity.from(history);
        return memberStatusHistoryJpaRepository.save(entity).toDomain();
    }
}
