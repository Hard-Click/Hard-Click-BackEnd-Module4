package com.wanted.backend.domain.identity.presentation.api;

import com.wanted.backend.domain.identity.application.service.LoginService;
import com.wanted.backend.domain.identity.domain.model.AuthToken;
import com.wanted.backend.domain.identity.presentation.api.request.LoginRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class IdentityController {

    private final LoginService loginService;

    @PostMapping("/login")
    public AuthToken login(@Valid @RequestBody LoginRequest request) {
        return loginService.login(request.getUsername(), request.getPassword());
    }

    @PostMapping("/refresh")
    public AuthToken refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        return loginService.refresh(refreshToken);
    }
}