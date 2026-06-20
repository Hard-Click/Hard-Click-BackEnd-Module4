package com.wanted.backend.domain.grass.infrastructure.config;

import com.wanted.backend.domain.grass.domain.policy.GrassLearningStatusPolicy;
import com.wanted.backend.domain.grass.domain.policy.LessonGrassLevelPolicy;
import com.wanted.backend.domain.grass.domain.policy.MonthlyGrassPeriodPolicy;
import com.wanted.backend.domain.grass.domain.policy.StudyTimeGrassLevelPolicy;
import com.wanted.backend.domain.grass.domain.policy.StudyStreakPolicy;
import com.wanted.backend.domain.grass.domain.policy.YearlyGrassPeriodPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrassPolicyConfig {

    @Bean
    public GrassLearningStatusPolicy grassLearningStatusPolicy() {
        return new GrassLearningStatusPolicy();
    }

    @Bean
    public StudyStreakPolicy studyStreakPolicy(GrassLearningStatusPolicy grassLearningStatusPolicy) {
        return new StudyStreakPolicy(grassLearningStatusPolicy);
    }

    @Bean
    public LessonGrassLevelPolicy lessonGrassLevelPolicy(GrassLessonProperties properties) {
        return new LessonGrassLevelPolicy(properties.maxLevel());
    }

    @Bean
    public StudyTimeGrassLevelPolicy studyTimeGrassLevelPolicy(GrassStudyTimeProperties properties) {
        return new StudyTimeGrassLevelPolicy(properties.levelThresholdSeconds());
    }

    @Bean
    public MonthlyGrassPeriodPolicy monthlyGrassPeriodPolicy() {
        return new MonthlyGrassPeriodPolicy();
    }

    @Bean
    public YearlyGrassPeriodPolicy yearlyGrassPeriodPolicy() {
        return new YearlyGrassPeriodPolicy();
    }
}
