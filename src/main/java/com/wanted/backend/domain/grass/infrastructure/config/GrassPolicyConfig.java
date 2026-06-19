package com.wanted.backend.domain.grass.infrastructure.config;

import com.wanted.backend.domain.grass.domain.policy.LessonGrassLevelPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrassPolicyConfig {

    @Bean
    public LessonGrassLevelPolicy lessonGrassLevelPolicy(GrassLessonProperties properties) {
        return new LessonGrassLevelPolicy(properties.maxLevel());
    }
}
