package com.wanted.backend.global.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.wanted.backend.domain.grass.application.usecase.GetGrassViewUseCase;
import com.wanted.backend.domain.grass.application.usecase.GetLessonGrassUseCase;
import com.wanted.backend.domain.grass.application.usecase.GetMonthlyGrassUseCase;
import com.wanted.backend.domain.grass.application.usecase.GetYearlyGrassUseCase;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper
    ) {
        ObjectMapper cacheObjectMapper = objectMapper.copy();
        // NON_FINAL은 record(=final) 루트 타입에 타입 메타데이터를 안 붙이는데,
        // GenericJackson2JsonRedisSerializer.deserialize(byte[])는 항상 Object.class를 기대해서
        // 캐시 HIT마다 역직렬화가 실패한다(WRAPPER_ARRAY 타입 정보 누락). EVERYTHING으로 모든 타입에 메타데이터를 강제한다.
        // allowIfBaseType(Object.class)는 모든 타입을 허용해 역직렬화 공격면을 키우므로,
        // 캐시에 실제로 들어가는 자체 DTO/record와 JDK 컬렉션·시간 타입으로 범위를 좁힌다.
        cacheObjectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType("com.wanted.backend")
                        .allowIfSubType("java.util")
                        .allowIfSubType("java.time")
                        .allowIfSubType("java.lang")
                        .build(),
                ObjectMapper.DefaultTyping.EVERYTHING
        );

        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer(cacheObjectMapper)
                        )
                );

        // 잔디 캐시는 캐시 이름별로 저장되는 타입이 이미 고정돼 있어서, 매번 타입 메타데이터(@class)를
        // 박아넣는 EVERYTHING 직렬화 대신 타입을 고정한 가벼운 직렬화를 쓴다.
        // 365일치를 통째로 캐싱하는 yearly/lessons는 페이로드가 커서, EVERYTHING 직렬화의
        // 역직렬화 비용이 캐시의 이득을 상회해 "캐시를 켰는데 더 느려지는" 역효과가 있었다(부하 측정으로 확인).
        Map<String, RedisCacheConfiguration> grassCacheConfigs = buildGrassCacheConfigs(configuration, objectMapper.copy());

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(configuration)
                .withInitialCacheConfigurations(grassCacheConfigs)
                .build();
    }

    /**
     * 잔디 캐시별 타입 고정 직렬화 설정을 만든다. CacheSerializationRoundTripTest가 이 메서드를
     * 직접 호출해서, 실제 캐시 매니저가 쓰는 것과 동일한 설정으로 라운드트립을 검증한다
     * (테스트에서 직렬화 설정을 따로 재구성하면 캐시 이름·타입이 어긋나도 테스트가 못 잡는다).
     */
    static Map<String, RedisCacheConfiguration> buildGrassCacheConfigs(
            RedisCacheConfiguration baseConfig,
            ObjectMapper plainObjectMapper
    ) {
        return Map.of(
                "grassMonthly:v3", baseConfig.serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new Jackson2JsonRedisSerializer<>(plainObjectMapper, GetMonthlyGrassUseCase.MonthlyGrassView.class)
                        )
                ),
                "grassYearly:v3", baseConfig.serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new Jackson2JsonRedisSerializer<>(plainObjectMapper, GetYearlyGrassUseCase.YearlyGrassView.class)
                        )
                ),
                "grassView:v3", baseConfig.serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new Jackson2JsonRedisSerializer<>(plainObjectMapper, GetGrassViewUseCase.GrassView.class)
                        )
                ),
                "grassLessons:v3", baseConfig.serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new Jackson2JsonRedisSerializer<>(
                                        plainObjectMapper,
                                        (JavaType) plainObjectMapper.getTypeFactory()
                                                .constructCollectionType(List.class, GetLessonGrassUseCase.LessonGrassView.class)
                                )
                        )
                )
        );
    }
}
