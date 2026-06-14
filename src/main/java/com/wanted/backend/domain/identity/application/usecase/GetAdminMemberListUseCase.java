package com.wanted.backend.domain.identity.application.usecase;

import com.wanted.backend.domain.identity.application.dto.AdminMemberListResult;
import com.wanted.backend.domain.identity.application.query.AdminMemberListQuery;

public interface GetAdminMemberListUseCase {

    AdminMemberListResult getList(AdminMemberListQuery query);
}