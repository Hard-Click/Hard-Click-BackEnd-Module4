package com.wanted.backend.domain.identity.presentation.api;

import com.wanted.backend.domain.identity.application.command.ChangeMemberStatusCommand;
import com.wanted.backend.domain.identity.application.dto.AdminMemberListResult;
import com.wanted.backend.domain.identity.application.dto.ChangeMemberStatusResult;
import com.wanted.backend.domain.identity.application.usecase.ChangeMemberStatusUseCase;
import com.wanted.backend.domain.identity.application.usecase.GetAdminMemberListUseCase;
import com.wanted.backend.domain.identity.presentation.api.request.AdminMemberListRequest;
import com.wanted.backend.domain.identity.presentation.api.request.ChangeMemberStatusRequest;
import com.wanted.backend.domain.identity.presentation.api.response.AdminMemberListResponse;
import com.wanted.backend.domain.identity.presentation.api.response.ChangeMemberStatusResponse;
import com.wanted.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
@Tag(name = "Admin Member", description = "관리자 회원 관리 API")
public class AdminMemberController {

    private final GetAdminMemberListUseCase getAdminMemberListUseCase;
    private final ChangeMemberStatusUseCase changeMemberStatusUseCase;

    @GetMapping
    @Operation(
            summary = "회원 목록/검색",
            description = "관리자가 회원 목록을 조회하고, 이름/아이디/이메일 검색 및 역할/상태 필터를 적용합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    public ResponseEntity<ApiResponse<AdminMemberListResponse>> getMembers(
            @Valid @ModelAttribute AdminMemberListRequest request
    ) {
        AdminMemberListResult result = getAdminMemberListUseCase.getList(request.toQuery());

        return ApiResponse.success(
                "회원 목록 조회 성공",
                AdminMemberListResponse.from(result)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{memberId}/status")
    @Operation(
            summary = "회원 커뮤니티 이용 제한 상태 변경",
            description = "관리자가 회원 상태를 ACTIVE 또는 SUSPENDED로 변경합니다. WITHDRAWN은 이 API에서 변경할 수 없습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 상태 변경 성공"),
            @ApiResponse(responseCode = "400", description = "변경 불가 상태 (WITHDRAWN 등)"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<ChangeMemberStatusResponse>> changeMemberStatus(
            @Parameter(description = "상태를 변경할 회원 ID", example = "1")
            @PathVariable Long memberId,
            @Valid @RequestBody ChangeMemberStatusRequest request
    ) {
        ChangeMemberStatusCommand command = new ChangeMemberStatusCommand(
                memberId,
                request.status(),
                request.memo()
        );
        ChangeMemberStatusResult result = changeMemberStatusUseCase.changeStatus(command);

        return ApiResponse.success(
                "회원 상태가 변경되었습니다.",
                ChangeMemberStatusResponse.from(result)
        );
    }
}
