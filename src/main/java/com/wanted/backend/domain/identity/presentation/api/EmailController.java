package com.wanted.backend.domain.identity.presentation.api;

import com.wanted.backend.domain.identity.application.usecase.VerifyEmailUseCase;
import com.wanted.backend.domain.identity.domain.model.EmailPurpose;
import com.wanted.backend.domain.identity.presentation.api.request.EmailSendRequest;
import com.wanted.backend.domain.identity.presentation.api.request.EmailVerifyRequest;
import com.wanted.backend.domain.identity.presentation.api.response.EmailVerifyResponse;
import com.wanted.backend.domain.identity.presentation.api.response.EmptyResponse;
import com.wanted.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Email Verification", description = "회원가입 이메일 인증 API")
@RestController
@RequestMapping("/api/auth/email")
@RequiredArgsConstructor
public class EmailController {

    private final VerifyEmailUseCase verifyEmailUseCase;


    @Operation(
            summary = "회원가입 이메일 인증번호 발송",
            description = "회원가입에 사용할 이메일로 인증번호를 발송합니다."
    )
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<EmptyResponse>> sendCode(
            @Valid @RequestBody EmailSendRequest request
    ) {
        verifyEmailUseCase.sendVerificationCode(request.email(), EmailPurpose.SIGNUP);
        return ApiResponse.success("이메일 인증번호가 발송되었습니다", new EmptyResponse());
    }


    @Operation(
            summary = "회원가입 이메일 인증번호 검증",
            description = "이메일로 발송된 인증번호를 검증하고 회원가입에 사용할 이메일 인증 토큰을 발급합니다."
    )
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<EmailVerifyResponse>> verifyCode(
            @Valid @RequestBody EmailVerifyRequest request
    ) {
        String token = verifyEmailUseCase.verifyCode(
                request.email(),
                request.code(),
                EmailPurpose.SIGNUP
        );

        return ApiResponse.success(
                "이메일 인증이 완료되었습니다",
                new EmailVerifyResponse(token)
        );
    }
}