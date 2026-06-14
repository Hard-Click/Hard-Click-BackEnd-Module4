package com.wanted.backend.domain.identity.application.port;

import com.wanted.backend.domain.identity.application.dto.AdminMemberListResult;
import com.wanted.backend.domain.identity.application.query.AdminMemberListQuery;

public interface AdminMemberQueryPort {

    AdminMemberListResult findMembers(AdminMemberListQuery query);
}