package com.wanted.backend.domain.identity.presentation.api.request;

import com.wanted.backend.domain.identity.application.query.AdminMemberListQuery;
import com.wanted.backend.domain.identity.domain.model.MemberStatus;
import com.wanted.backend.domain.identity.domain.model.Role;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class AdminMemberListRequest {

    private String keyword;
    private Role role;
    private MemberStatus status;

    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
    private int page = 0;

    @Min(value = 1, message = "조회 크기는 1 이상이어야 합니다.")
    @Max(value = 100, message = "조회 크기는 100 이하여야 합니다.")
    private int size = 20;

    public String getKeyword() {
        return keyword;
    }

    public Role getRole() {
        return role;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setStatus(MemberStatus status) {
        this.status = status;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public AdminMemberListQuery toQuery() {
        return new AdminMemberListQuery(keyword, role, status, page, size);
    }
}