package com.wanted.backend.domain.identity.presentation.api;

import com.wanted.backend.domain.identity.application.usecase.VerifyEmailUseCase;
import com.wanted.backend.domain.identity.domain.model.EmailPurpose;
import com.wanted.backend.domain.identity.presentation.api.request.EmailSendRequest;
import com.wanted.backend.domain.identity.presentation.api.request.EmailVerifyRequest;
import com.wanted.backend.domain.identity.presentation.api.response.EmailVerifyResponse;
import com.wanted.backend.domain.identity.presentation.api.response.EmptyResponse;
import com.wanted.backend.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/email")
@RequiredArgsConstructor
public class EmailController {

    private final VerifyEmailUseCase verifyEmailUseCase;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<EmptyResponse>> sendCode(
            @Valid @RequestBody EmailSendRequest request
    ) {
        verifyEmailUseCase.sendVerificationCode(request.getEmail(), EmailPurpose.SIGNUP);
        return ApiResponse.success("이메일 인증번호가 발송되었습니다", new EmptyResponse());
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<EmailVerifyResponse>> verifyCode(
            @Valid @RequestBody EmailVerifyRequest request
    ) {
        String token = verifyEmailUseCase.verifyCode(
                request.getEmail(),
                request.getCode(),
                EmailPurpose.SIGNUP
        );

        return ApiResponse.success(
                "이메일 인증이 완료되었습니다",
                new EmailVerifyResponse(token)
        );
    }
}