package com.wanted.backend.domain.identity.presentation.api;

import com.wanted.backend.domain.identity.application.dto.AdminMemberListResult;
import com.wanted.backend.domain.identity.application.usecase.GetAdminMemberListUseCase;
import com.wanted.backend.domain.identity.presentation.api.request.AdminMemberListRequest;
import com.wanted.backend.domain.identity.presentation.api.response.AdminMemberListResponse;
import com.wanted.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
@Tag(name = "Admin Member", description = "관리자 회원 관리 API")
public class AdminMemberController {

    private final GetAdminMemberListUseCase getAdminMemberListUseCase;

    @GetMapping
    @Operation(
            summary = "회원 목록/검색",
            description = "관리자가 회원 목록을 조회하고, 이름/아이디/이메일 검색 및 역할/상태 필터를 적용합니다."
    )
    public ResponseEntity<ApiResponse<AdminMemberListResponse>> getMembers(
            @Valid @ModelAttribute AdminMemberListRequest request
    ) {
        AdminMemberListResult result = getAdminMemberListUseCase.getList(request.toQuery());

        return ApiResponse.success(
                "회원 목록 조회 성공",
                AdminMemberListResponse.from(result)
        );
    }
}