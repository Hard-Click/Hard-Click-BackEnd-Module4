package com.wanted.backend.domain.identity.presentation.api;

import com.wanted.backend.domain.identity.application.command.AccountLockPasswordChangeCommand;
import com.wanted.backend.domain.identity.application.command.AccountLockVerifyCommand;
import com.wanted.backend.domain.identity.application.usecase.AccountLockUseCase;
import com.wanted.backend.domain.identity.presentation.api.request.AccountLockPasswordChangeRequest;
import com.wanted.backend.domain.identity.presentation.api.request.AccountLockVerifyRequest;
import com.wanted.backend.domain.identity.presentation.api.response.EmptyResponse;
import com.wanted.backend.domain.identity.presentation.api.response.PasswordChangeTokenResponse;
import com.wanted.backend.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/account-locks")
@RequiredArgsConstructor
public class AccountLockController {

    private final AccountLockUseCase accountLockUseCase;

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<PasswordChangeTokenResponse>> verify(
            @Valid @RequestBody AccountLockVerifyRequest request
    ) {
        String token = accountLockUseCase.verify(new AccountLockVerifyCommand(
                request.getEmail(),
                request.getCode()
        ));

        return ApiResponse.success(
                "계정 보호 인증이 완료되었습니다",
                new PasswordChangeTokenResponse(token)
        );
    }

    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<EmptyResponse>> changePassword(
            @Valid @RequestBody AccountLockPasswordChangeRequest request
    ) {
        accountLockUseCase.changePassword(new AccountLockPasswordChangeCommand(
                request.getPasswordChangeToken(),
                request.getNewPassword(),
                request.getNewPasswordConfirm()
        ));

        return ApiResponse.success(
                "비밀번호가 변경되고 계정 잠금이 해제되었습니다",
                new EmptyResponse()
        );
    }
}