package com.wanted.backend.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/uploads/community/post/**")
                .addResourceLocations("file:C:/lecture/Hard-Click-B/Hard-Click-BackEnd/src/main/resources/static/community/post/");

        registry.addResourceHandler("/uploads/community/comment/**")
                .addResourceLocations("file:C:/lecture/Hard-Click-B/Hard-Click-BackEnd/src/main/resources/static/community/comment/");
    }
}
