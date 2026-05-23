package com.wanted.backend.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // 게시글 이미지
        registry.addResourceHandler("/community/post/**")
                .addResourceLocations("file:src/main/resources/static/community/post/");

        // 댓글 이미지
        registry.addResourceHandler("/community/comment/**")
                .addResourceLocations("file:src/main/resources/static/community/comment/");
    }
}