package com.wanted.backend.domain.identity.presentation.api;

import com.wanted.backend.domain.identity.application.command.ResetPasswordCommand;
import com.wanted.backend.domain.identity.application.usecase.ResetPasswordUseCase;
import com.wanted.backend.domain.identity.application.usecase.VerifyEmailUseCase;
import com.wanted.backend.domain.identity.domain.model.EmailPurpose;
import com.wanted.backend.domain.identity.presentation.api.request.PasswordResetEmailRequest;
import com.wanted.backend.domain.identity.presentation.api.request.PasswordResetVerifyRequest;
import com.wanted.backend.domain.identity.presentation.api.request.ResetPasswordRequest;
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
@RequestMapping("/api/auth/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final VerifyEmailUseCase verifyEmailUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;

    @PostMapping("/email")
    public ResponseEntity<ApiResponse<EmptyResponse>> sendResetCode(
            @Valid @RequestBody PasswordResetEmailRequest request
    ) {
        verifyEmailUseCase.sendPasswordResetCode(request.getEmail());

        return ApiResponse.success(
                "비밀번호 재설정 인증번호가 발송되었습니다",
                new EmptyResponse()
        );
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<PasswordChangeTokenResponse>> verifyResetCode(
            @Valid @RequestBody PasswordResetVerifyRequest request
    ) {
        String token = verifyEmailUseCase.verifyCode(
                request.getEmail(),
                request.getCode(),
                EmailPurpose.PASSWORD_RESET
        );

        return ApiResponse.success(
                "인증번호 검증이 완료되었습니다",
                new PasswordChangeTokenResponse(token)
        );
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<EmptyResponse>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        resetPasswordUseCase.resetPassword(new ResetPasswordCommand(
                request.getEmail(),
                request.getPasswordChangeToken(),
                request.getNewPassword(),
                request.getNewPasswordConfirm()
        ));

        return ApiResponse.success(
                "비밀번호가 성공적으로 재설정되었습니다",
                new EmptyResponse()
        );
    }
}