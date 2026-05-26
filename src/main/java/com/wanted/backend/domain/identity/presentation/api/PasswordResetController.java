package com.wanted.backend.domain.identity.presentation.api;

import com.wanted.backend.domain.identity.application.usecase.ResetPasswordUseCase;
import com.wanted.backend.domain.identity.application.usecase.VerifyEmailUseCase;
import com.wanted.backend.domain.identity.domain.model.EmailPurpose;
import com.wanted.backend.domain.identity.presentation.api.request.PasswordResetEmailRequest;
import com.wanted.backend.domain.identity.presentation.api.request.PasswordResetVerifyRequest;
import com.wanted.backend.domain.identity.presentation.api.request.ResetPasswordRequest;
import com.wanted.backend.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
    @RequestMapping("/api/auth/password-reset") // 1. 상위 경로
    @RequiredArgsConstructor
    public class PasswordResetController {

    private final VerifyEmailUseCase verifyEmailUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;

    @PostMapping("/email")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendResetCode(
            @Valid @RequestBody PasswordResetEmailRequest request
    ) {
        verifyEmailUseCase.sendPasswordResetCode(request.getEmail());
        return ApiResponse.success("비밀번호 재설정 인증번호가 발송되었습니다", Map.of());
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Map<String, String>>> verifyResetCode(
            @Valid @RequestBody PasswordResetVerifyRequest request
    ) {
        // 기존 검증 로직 재사용 (용도는 PASSWORD_RESET)
        String token = verifyEmailUseCase.verifyCode(request.getEmail(), request.getCode(), EmailPurpose.PASSWORD_RESET);

        return ApiResponse.success("인증번호 검증이 완료되었습니다", Map.of("passwordChangeToken", token));
    }
    @PatchMapping // 2. PATCH /api/auth/password-reset
    public ResponseEntity<ApiResponse<Map<String, Object>>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        resetPasswordUseCase.resetPassword(request);
        return ApiResponse.success("비밀번호가 성공적으로 재설정되었습니다", Map.of());
    }
    }

