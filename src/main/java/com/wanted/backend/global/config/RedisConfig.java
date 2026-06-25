package com.wanted.backend.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

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

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(configuration)
                .build();
    }
}
