package com.wanted.backend.domain.identity.presentation.api.request;

import com.wanted.backend.domain.identity.application.query.AdminMemberListQuery;
import com.wanted.backend.domain.identity.domain.model.MemberStatus;
import com.wanted.backend.domain.identity.domain.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "관리자 회원 목록 조회 요청")
public class AdminMemberListRequest {

    @Schema(description = "검색어 (이름, 아이디, 이메일)", example = "홍길동")
    private String keyword;

    @Schema(description = "역할 필터 (STUDENT, INSTRUCTOR, ADMIN)", example = "STUDENT")
    private Role role;

    @Schema(description = "상태 필터 (ACTIVE, SUSPENDED, WITHDRAWN)", example = "ACTIVE")
    private MemberStatus status;

    @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
    private int page = 0;

    @Schema(description = "페이지당 조회 수 (1~100)", example = "10")
    @Min(value = 1, message = "조회 크기는 1 이상이어야 합니다.")
    @Max(value = 100, message = "조회 크기는 100 이하여야 합니다.")
    private int size = 3;

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
