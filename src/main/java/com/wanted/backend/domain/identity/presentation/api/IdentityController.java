package com.wanted.backend.domain.identity.presentation.api;

import com.wanted.backend.domain.identity.application.service.LoginService;
import com.wanted.backend.domain.identity.application.usecase.CheckDuplicateUseCase;
import com.wanted.backend.domain.identity.domain.model.AuthToken;
import com.wanted.backend.domain.identity.presentation.api.request.LoginRequest;
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

    @PostMapping("/login")
    public AuthToken login(@Valid @RequestBody LoginRequest request) {
        return loginService.login(request.getUsername(), request.getPassword());
    }

    @PostMapping("/refresh")
    public AuthToken refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        return loginService.refresh(refreshToken);
    }
    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<Boolean>> checkUsername(@RequestParam String username) {
        boolean isDuplicated = checkDuplicateUseCase.isUsernameDuplicated(username);
        return ApiResponse.success("아이디 중복 확인 결과입니다.", isDuplicated);
    }

    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(@RequestParam String email) {
        boolean isDuplicated = checkDuplicateUseCase.isEmailDuplicated(email);
        return ApiResponse.success("이메일 중복 확인 결과입니다.", isDuplicated);
    }
}