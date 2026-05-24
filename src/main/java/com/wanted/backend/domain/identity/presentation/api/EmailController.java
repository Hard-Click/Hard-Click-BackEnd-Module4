package com.wanted.backend.domain.identity.presentation.api;

import com.wanted.backend.domain.identity.application.usecase.VerifyEmailUseCase;
import com.wanted.backend.domain.identity.domain.model.EmailPurpose;
import com.wanted.backend.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

    @RestController
    @RequestMapping("/api/auth/email")
    @RequiredArgsConstructor
    public class EmailController {

    private final VerifyEmailUseCase verifyEmailUseCase;

    // 인증 번호 발송 API
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        verifyEmailUseCase.sendVerificationCode(email, EmailPurpose.SIGNUP);
        return ApiResponse.success("이메일 인증번호가 발송되었습니다", Map.of());
    }

    // 인증 번호 검증 API
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Map<String, String>>> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        String token = verifyEmailUseCase.verifyCode(email, code, EmailPurpose.SIGNUP);
        return ApiResponse.success("이메일 인증이 완료되었습니다", Map.of("emailVerificationToken", token));
    }
}