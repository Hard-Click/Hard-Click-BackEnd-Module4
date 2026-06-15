package com.wanted.backend.domain.identity.domain.repository;

import com.wanted.backend.domain.identity.domain.model.MemberStatusHistory;

public interface MemberStatusHistoryRepository {
    MemberStatusHistory save(MemberStatusHistory history);
}
