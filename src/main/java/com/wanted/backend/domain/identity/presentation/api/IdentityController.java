package com.wanted.backend.domain.identity.presentation.api;

import com.wanted.backend.domain.identity.application.command.SignupCommand;
import com.wanted.backend.domain.identity.application.usecase.CheckDuplicateUseCase;
import com.wanted.backend.domain.identity.application.usecase.LoginUseCase;
import com.wanted.backend.domain.identity.application.usecase.LogoutUseCase;
import com.wanted.backend.domain.identity.application.usecase.SignupUseCase;
import com.wanted.backend.domain.identity.domain.model.AuthToken;
import com.wanted.backend.domain.identity.presentation.api.request.LoginRequest;
import com.wanted.backend.domain.identity.presentation.api.request.LogoutRequest;
import com.wanted.backend.domain.identity.presentation.api.request.RefreshTokenRequest;
import com.wanted.backend.domain.identity.presentation.api.request.SignupRequest;
import com.wanted.backend.domain.identity.presentation.api.response.*;
import com.wanted.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Identity", description = "인증 및 회원가입 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class IdentityController {

    private final LoginUseCase loginUseCase;
    private final CheckDuplicateUseCase checkDuplicateUseCase;
    private final SignupUseCase signupUseCase;
    private final LogoutUseCase logoutUseCase;


    @Operation(
            summary = "로그인",
            description = "아이디와 비밀번호로 로그인하고 Access Token과 Refresh Token을 발급합니다."
    )
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthToken token = loginUseCase.login(request.username(), request.password());

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

    @Operation(
            summary = "Access Token 재발급",
            description = "Refresh Token을 검증하고 새로운 Access Token을 발급합니다."
    )
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthToken token = loginUseCase.refresh(request.refreshToken());

        return ApiResponse.success(
                "Access Token이 재발급되었습니다",
                new RefreshTokenResponse(token.accessToken())
        );
    }

    @Operation(
            summary = "아이디 중복 확인",
            description = "회원가입에 사용할 아이디가 이미 사용 중인지 확인합니다."
    )
    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<DuplicateCheckResponse>> checkUsername(
            @Parameter(description = "중복 확인할 아이디", example = "testuser")
            @RequestParam String username) {
        boolean isDuplicated = checkDuplicateUseCase.isUsernameDuplicated(username);
        String message = isDuplicated ? "사용이 불가능한 아이디입니다" : "사용 가능한 아이디입니다";

        return ApiResponse.success(message, new DuplicateCheckResponse(isDuplicated));
    }

    @Operation(
            summary = "이메일 중복 확인",
            description = "회원가입에 사용할 이메일이 이미 사용 중인지 확인합니다."
    )
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<DuplicateCheckResponse>> checkEmail(

            @Parameter(description = "중복 확인할 이메일", example = "user@example.com")
            @RequestParam String email) {

        boolean isDuplicated = checkDuplicateUseCase.isEmailDuplicated(email);
        String message = isDuplicated ? "사용이 불가능한 이메일입니다" : "사용 가능한 이메일입니다";

        return ApiResponse.success(message, new DuplicateCheckResponse(isDuplicated));
    }

    @Operation(
            summary = "회원가입",
            description = "이메일 인증을 완료한 사용자 정보를 기반으로 회원가입을 처리합니다."
    )
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        Long memberId = signupUseCase.signup(new SignupCommand(
                request.username(),
                request.email(),
                request.password(),
                request.name(),
                request.gender(),
                request.birthDate(),
                request.phoneNumber(),
                request.profileImageUrl(),
                request.emailVerificationToken(),
                request.optionalTermsAgreed()
        ));

        return ApiResponse.created(
                "회원가입이 완료되었습니다",
                new SignupResponse(memberId)
        );
    }

    @Operation(
            summary = "로그아웃",
            description = "Refresh Token을 삭제하여 로그아웃 처리합니다."
    )
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<EmptyResponse>> logout(@Valid @RequestBody LogoutRequest request) {
        logoutUseCase.logout(request.refreshToken());

        return ApiResponse.success("로그아웃되었습니다", new EmptyResponse());
    }
}