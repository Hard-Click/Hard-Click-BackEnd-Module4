package com.wanted.backend.domain.identity.presentation.api;

import com.wanted.backend.domain.identity.application.service.LoginService;
import com.wanted.backend.domain.identity.application.usecase.CheckDuplicateUseCase;
import com.wanted.backend.domain.identity.application.usecase.SignupUseCase;
import com.wanted.backend.domain.identity.domain.model.AuthToken;
import com.wanted.backend.domain.identity.presentation.api.request.LoginRequest;
import com.wanted.backend.domain.identity.presentation.api.request.SignupRequest;
import com.wanted.backend.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class IdentityController {

    private final LoginService loginService;
    private final CheckDuplicateUseCase checkDuplicateUseCase;
    private final SignupUseCase signupUseCase;
    @PostMapping("/login")
    public AuthToken login(@Valid @RequestBody LoginRequest request) {
        return loginService.login(request.getUsername(), request.getPassword());
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        AuthToken token = loginService.refresh(refreshToken);
        return ApiResponse.success("Access Token이 재발급되었습니다", Map.of("accessToken", token.accessToken()));
    }
    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkUsername(@RequestParam String username) {
        boolean isDuplicated = checkDuplicateUseCase.isUsernameDuplicated(username);
        String message = isDuplicated ? "사용이 불가능한 아이디입니다" : "사용 가능한 아이디입니다";
        return ApiResponse.success(message, Map.of("exists", isDuplicated));
    }

    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkEmail(@RequestParam String email) {
        boolean isDuplicated = checkDuplicateUseCase.isEmailDuplicated(email);
        String message = isDuplicated ? "사용이 불가능한 이메일입니다" : "사용 가능한 이메일입니다";
        return ApiResponse.success(message, Map.of("exists", isDuplicated));
    }
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Map<String, Long>>> signup(@Valid @RequestBody SignupRequest request) {
        Long memberId = signupUseCase.signup(request);
        return ApiResponse.created("회원가입이 완료되었습니다", Map.of("memberId", memberId));
    }
}