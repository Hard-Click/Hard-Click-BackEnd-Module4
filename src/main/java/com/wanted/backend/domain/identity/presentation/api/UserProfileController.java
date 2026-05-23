package com.wanted.backend.domain.identity.presentation.api;

import com.wanted.backend.domain.identity.application.usecase.GetMyProfileUseCase;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "User Profile", description = "마이페이지 프로필 API")
public class UserProfileController {

    private final GetMyProfileUseCase getMyProfileUseCase;

    @GetMapping("/me")
    @Operation(
            summary = "내 프로필 조회",
            description = "로그인한 사용자의 아이디, 이름, 이메일, 프로필 이미지를 조회합니다."
    )
    public ResponseEntity<ApiResponse<GetMyProfileUseCase.MyProfileView>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.success(
                "내 프로필이 조회되었습니다.",
                getMyProfileUseCase.handle(userDetails.getMemberId())
        );
    }
}
