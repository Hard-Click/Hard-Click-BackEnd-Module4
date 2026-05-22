package com.wanted.backend.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Hard-Click Backend API",
                version = "v1",
                description = "온라인 강의 수강, 영상 학습 활동, 회원 인증, 결제 및 커뮤니티 기능을 위한 Hard-Click 백엔드 API 명세"
        )
)
public class OpenApiConfig {
}
