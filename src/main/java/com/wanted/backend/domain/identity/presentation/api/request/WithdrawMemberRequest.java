package com.wanted.backend.domain.identity.presentation.api.request;

import com.wanted.backend.domain.identity.application.command.WithdrawMemberCommand;
import jakarta.validation.constraints.NotBlank;

public record WithdrawMemberRequest(
        @NotBlank(message = "현재 비밀번호를 입력해주세요")
        String currentPassword
) {
    public WithdrawMemberCommand toCommand() {
        return new WithdrawMemberCommand(currentPassword);
    }
}