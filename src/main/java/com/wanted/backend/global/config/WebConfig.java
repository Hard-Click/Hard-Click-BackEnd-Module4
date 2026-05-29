package com.wanted.backend.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // 업로드된 파일 (썸네일, 커뮤니티 이미지, 프로필 등)
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");

        //게시글 이미지
        registry.addResourceHandler("/community/post/**")
                .addResourceLocations("file:C:/lecture/Hard-Click-B/Hard-Click-BackEnd/src/main/resources/static/community/post/");

        //댓글 이미지
        registry.addResourceHandler("/community/comment/**")
                .addResourceLocations("file:C:/lecture/Hard-Click-B/Hard-Click-BackEnd/src/main/resources/static/community/comment/");

        // 프로필 이미지
        registry.addResourceHandler("/identity/profile/**")
                .addResourceLocations("file:src/main/resources/static/identity/profile/");
    }
}
