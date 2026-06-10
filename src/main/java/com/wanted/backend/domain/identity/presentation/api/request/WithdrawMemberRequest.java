package com.wanted.backend.domain.identity.presentation.api.request;

import com.wanted.backend.domain.identity.application.command.WithdrawMemberCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "회원 탈퇴 요청")
public record WithdrawMemberRequest(
        @Schema(description = "회원 탈퇴 확인용 현재 비밀번호", example = "Password123!")
        @NotBlank(message = "현재 비밀번호를 입력해주세요")
        String currentPassword
) {
    public WithdrawMemberCommand toCommand() {
        return new WithdrawMemberCommand(currentPassword);
    }
}