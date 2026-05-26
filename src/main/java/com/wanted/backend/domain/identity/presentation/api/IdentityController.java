package com.wanted.backend.domain.identity.presentation.api;

import com.wanted.backend.domain.identity.application.service.LoginService;
import com.wanted.backend.domain.identity.application.usecase.CheckDuplicateUseCase;
import com.wanted.backend.domain.identity.application.usecase.LogoutUseCase;
import com.wanted.backend.domain.identity.application.usecase.SignupUseCase;
import com.wanted.backend.domain.identity.application.usecase.UpdatePasswordUseCase;
import com.wanted.backend.domain.identity.domain.model.AuthToken;
import com.wanted.backend.domain.identity.presentation.api.request.LoginRequest;
import com.wanted.backend.domain.identity.presentation.api.request.LogoutRequest;
import com.wanted.backend.domain.identity.presentation.api.request.RefreshTokenRequest;
import com.wanted.backend.domain.identity.presentation.api.request.SignupRequest;
import com.wanted.backend.domain.identity.presentation.api.response.*;
import com.wanted.backend.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class IdentityController {

    private final LoginService loginService;
    private final CheckDuplicateUseCase checkDuplicateUseCase;
    private final SignupUseCase signupUseCase;
    private final LogoutUseCase logoutUseCase;
    private final UpdatePasswordUseCase updatePasswordUseCase;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthToken token = loginService.login(request.getUsername(), request.getPassword());

        return ApiResponse.success(
                "로그인에 성공했습니다",
                new LoginResponse(
                        token.accessToken(),
                        token.refreshToken(),
                        token.memberId(),
                        token.role()
                )
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refresh(@RequestBody RefreshTokenRequest request) {
        AuthToken token = loginService.refresh(request.getRefreshToken());

        return ApiResponse.success(
                "Access Token이 재발급되었습니다",
                new RefreshTokenResponse(token.accessToken())
        );
    }

    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<DuplicateCheckResponse>> checkUsername(@RequestParam String username) {
        boolean isDuplicated = checkDuplicateUseCase.isUsernameDuplicated(username);
        String message = isDuplicated ? "사용이 불가능한 아이디입니다" : "사용 가능한 아이디입니다";

        return ApiResponse.success(message, new DuplicateCheckResponse(isDuplicated));
    }

    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<DuplicateCheckResponse>> checkEmail(@RequestParam String email) {
        boolean isDuplicated = checkDuplicateUseCase.isEmailDuplicated(email);
        String message = isDuplicated ? "사용이 불가능한 이메일입니다" : "사용 가능한 이메일입니다";

        return ApiResponse.success(message, new DuplicateCheckResponse(isDuplicated));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        Long memberId = signupUseCase.signup(request);

        return ApiResponse.created(
                "회원가입이 완료되었습니다",
                new SignupResponse(memberId)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<EmptyResponse>> logout(@RequestBody LogoutRequest request) {
        logoutUseCase.logout(request.getRefreshToken());

        return ApiResponse.success("로그아웃되었습니다", new EmptyResponse());
    }
}