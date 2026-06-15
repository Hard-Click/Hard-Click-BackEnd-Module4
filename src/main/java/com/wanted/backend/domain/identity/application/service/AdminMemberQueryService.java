package com.wanted.backend.domain.identity.application.service;

import com.wanted.backend.domain.identity.application.dto.AdminMemberListResult;
import com.wanted.backend.domain.identity.application.port.AdminMemberQueryPort;
import com.wanted.backend.domain.identity.application.query.AdminMemberListQuery;
import com.wanted.backend.domain.identity.application.usecase.GetAdminMemberListUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemberQueryService implements GetAdminMemberListUseCase {

    private final AdminMemberQueryPort adminMemberQueryPort;

    @Override
    public AdminMemberListResult getList(AdminMemberListQuery query) {
        return adminMemberQueryPort.findMembers(query);
    }
}