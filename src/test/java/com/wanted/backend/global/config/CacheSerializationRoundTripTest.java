package com.wanted.backend.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wanted.backend.domain.grass.application.mapper.MonthlyGrassViewMapper;
import com.wanted.backend.domain.grass.application.mapper.YearlyGrassViewMapper;
import com.wanted.backend.domain.grass.application.query.GetGrassViewQuery;
import com.wanted.backend.domain.grass.application.query.GetLessonGrassQuery;
import com.wanted.backend.domain.grass.application.query.GetMonthlyGrassQuery;
import com.wanted.backend.domain.grass.application.query.GetYearlyGrassQuery;
import com.wanted.backend.domain.grass.application.service.GetGrassViewService;
import com.wanted.backend.domain.grass.application.service.GetLessonGrassService;
import com.wanted.backend.domain.grass.application.service.GetMonthlyGrassService;
import com.wanted.backend.domain.grass.application.service.GetYearlyGrassService;
import com.wanted.backend.domain.grass.application.usecase.GetGrassViewUseCase;
import com.wanted.backend.domain.grass.application.usecase.GetLessonGrassUseCase;
import com.wanted.backend.domain.grass.application.usecase.GetMonthlyGrassUseCase;
import com.wanted.backend.domain.grass.application.usecase.GetYearlyGrassUseCase;
import com.wanted.backend.domain.grass.domain.model.LessonGrassStat;
import com.wanted.backend.domain.grass.domain.model.YearlyGrassStat;
import com.wanted.backend.domain.grass.domain.policy.GrassViewModePolicy;
import com.wanted.backend.domain.grass.domain.policy.LessonGrassLevelPolicy;
import com.wanted.backend.domain.grass.domain.policy.MonthlyGrassPeriodPolicy;
import com.wanted.backend.domain.grass.domain.policy.YearlyGrassPeriodPolicy;
import com.wanted.backend.domain.grass.domain.repository.LessonGrassRepository;
import com.wanted.backend.domain.grass.domain.repository.MonthlyGrassRepository;
import com.wanted.backend.domain.grass.domain.repository.YearlyGrassRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * RedisConfig.cacheManager()와 동일한 ObjectMapper(activateDefaultTyping EVERYTHING) 설정으로
 * 실제 캐싱 대상 응답 객체가 직렬화 -> 역직렬화 라운드트립에서 깨지지 않는지 검증한다.
 *
 * record(=final) 타입을 @Cacheable의 루트 반환값으로 쓰면, DefaultTyping.NON_FINAL에서는
 * 타입 메타데이터가 안 붙는데 GenericJackson2JsonRedisSerializer.deserialize(byte[])는
 * 항상 Object.class를 기대해서 캐시 HIT마다 역직렬화가 실패했다
 * (grass monthly/yearly/lessons, admin dashboard 500의 원인). EVERYTHING으로 바꿔서 해결.
 */
class CacheSerializationRoundTripTest {

    private final GenericJackson2JsonRedisSerializer serializer = buildCacheSerializer();

    private static GenericJackson2JsonRedisSerializer buildCacheSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType("com.wanted.backend")
                        .allowIfSubType("java.util")
                        .allowIfSubType("java.time")
                        .allowIfSubType("java.lang")
                        .build(),
                ObjectMapper.DefaultTyping.EVERYTHING
        );
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    @Test
    void roundTripsLessonGrassViewList() {
        LessonGrassRepository repository = mock(LessonGrassRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-01-03T00:00:00Z"), ZoneId.of("Asia/Seoul"));
        GetLessonGrassService service = new GetLessonGrassService(repository, new LessonGrassLevelPolicy(4), new YearlyGrassPeriodPolicy(), clock);
        when(repository.findByMemberIdAndDateBetween(1L, LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-03")))
                .thenReturn(List.of(new LessonGrassStat(1L, LocalDate.parse("2026-01-02"), 3)));

        List<GetLessonGrassUseCase.LessonGrassView> original = service.handle(new GetLessonGrassQuery(1L, null));

        assertRoundTrips(original);
    }

    @Test
    void roundTripsMonthlyGrassView() {
        MonthlyGrassRepository repository = mock(MonthlyGrassRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-06-15T00:00:00Z"), ZoneId.of("Asia/Seoul"));
        MonthlyGrassViewMapper mapper = new MonthlyGrassViewMapper(new LessonGrassLevelPolicy(4));
        GetMonthlyGrassService service = new GetMonthlyGrassService(
                repository, mapper, new MonthlyGrassPeriodPolicy(), clock
        );
        when(repository.findByMemberIdAndDateBetween(1L, LocalDate.parse("2026-06-01"), LocalDate.parse("2026-06-15")))
                .thenReturn(List.of());

        GetMonthlyGrassUseCase.MonthlyGrassView original = service.handle(new GetMonthlyGrassQuery(1L, 2026, 6));

        assertRoundTrips(original);
    }

    @Test
    void roundTripsYearlyGrassView() {
        YearlyGrassRepository repository = mock(YearlyGrassRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-03-10T00:00:00Z"), ZoneId.of("Asia/Seoul"));
        YearlyGrassViewMapper mapper = new YearlyGrassViewMapper(new LessonGrassLevelPolicy(4));
        GetYearlyGrassService service = new GetYearlyGrassService(
                repository, mapper, new YearlyGrassPeriodPolicy(), clock
        );
        when(repository.findByMemberIdAndDateBetween(1L, LocalDate.parse("2026-01-01"), LocalDate.parse("2026-03-10")))
                .thenReturn(List.of(new YearlyGrassStat(1L, LocalDate.parse("2026-02-01"), 2)));

        GetYearlyGrassUseCase.YearlyGrassView original = service.handle(new GetYearlyGrassQuery(1L, 2026));

        assertRoundTrips(original);
    }

    @Test
    void roundTripsCombinedGrassView() {
        GetMonthlyGrassUseCase monthlyUseCase = mock(GetMonthlyGrassUseCase.class);
        GetYearlyGrassUseCase yearlyUseCase = mock(GetYearlyGrassUseCase.class);
        GetGrassViewService service = new GetGrassViewService(monthlyUseCase, yearlyUseCase, new GrassViewModePolicy());
        when(monthlyUseCase.handle(any(GetMonthlyGrassQuery.class))).thenReturn(new GetMonthlyGrassUseCase.MonthlyGrassView(
                2026, 6, List.of(new GetMonthlyGrassUseCase.MonthlyGrassDayView(
                        LocalDate.parse("2026-06-01"), 2, 2, false
                ))
        ));

        GetGrassViewUseCase.GrassView original = service.handle(new GetGrassViewQuery(1L, "monthly", 2026, 6));

        assertRoundTrips(original);
    }

    // AdminDashboardResult는 AdminDashboardQueryAdapter(JPA 쿼리)를 실제로 거쳐야
    // findRecentReports()/findRecentNotices()의 ArrayList 수집 로직까지 검증되므로,
    // AdminDashboardQueryAdapterCacheSerializationTest(@DataJpaTest)에서 다룬다.

    /**
     * RedisConfig가 grassMonthly/grassYearly/grassView/grassLessons 캐시에 쓰는
     * 타입 고정 Jackson2JsonRedisSerializer도 EVERYTHING 직렬화와 동일하게
     * 라운드트립이 깨지지 않는지 검증한다 (성능 개선용 직렬화 교체이지, 정합성을 깨면 안 됨).
     */
    @Test
    void typedSerializerRoundTripsMonthlyGrassView() {
        MonthlyGrassRepository repository = mock(MonthlyGrassRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-06-15T00:00:00Z"), ZoneId.of("Asia/Seoul"));
        MonthlyGrassViewMapper mapper = new MonthlyGrassViewMapper(new LessonGrassLevelPolicy(4));
        GetMonthlyGrassService service = new GetMonthlyGrassService(
                repository, mapper, new MonthlyGrassPeriodPolicy(), clock
        );
        when(repository.findByMemberIdAndDateBetween(1L, LocalDate.parse("2026-06-01"), LocalDate.parse("2026-06-15")))
                .thenReturn(List.of());

        GetMonthlyGrassUseCase.MonthlyGrassView original = service.handle(new GetMonthlyGrassQuery(1L, 2026, 6));

        assertRoundTripsTyped(original, GetMonthlyGrassUseCase.MonthlyGrassView.class);
    }

    @Test
    void typedSerializerRoundTripsYearlyGrassView() {
        YearlyGrassRepository repository = mock(YearlyGrassRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-03-10T00:00:00Z"), ZoneId.of("Asia/Seoul"));
        YearlyGrassViewMapper mapper = new YearlyGrassViewMapper(new LessonGrassLevelPolicy(4));
        GetYearlyGrassService service = new GetYearlyGrassService(
                repository, mapper, new YearlyGrassPeriodPolicy(), clock
        );
        when(repository.findByMemberIdAndDateBetween(1L, LocalDate.parse("2026-01-01"), LocalDate.parse("2026-03-10")))
                .thenReturn(List.of(new YearlyGrassStat(1L, LocalDate.parse("2026-02-01"), 2)));

        GetYearlyGrassUseCase.YearlyGrassView original = service.handle(new GetYearlyGrassQuery(1L, 2026));

        assertRoundTripsTyped(original, GetYearlyGrassUseCase.YearlyGrassView.class);
    }

    @Test
    void typedSerializerRoundTripsGrassView() {
        GetMonthlyGrassUseCase monthlyUseCase = mock(GetMonthlyGrassUseCase.class);
        GetYearlyGrassUseCase yearlyUseCase = mock(GetYearlyGrassUseCase.class);
        GetGrassViewService service = new GetGrassViewService(monthlyUseCase, yearlyUseCase, new GrassViewModePolicy());
        when(monthlyUseCase.handle(any(GetMonthlyGrassQuery.class))).thenReturn(new GetMonthlyGrassUseCase.MonthlyGrassView(
                2026, 6, List.of(new GetMonthlyGrassUseCase.MonthlyGrassDayView(
                        LocalDate.parse("2026-06-01"), 2, 2, false
                ))
        ));

        GetGrassViewUseCase.GrassView original = service.handle(new GetGrassViewQuery(1L, "monthly", 2026, 6));

        assertRoundTripsTyped(original, GetGrassViewUseCase.GrassView.class);
    }

    @Test
    void typedSerializerRoundTripsLessonGrassViewList() {
        LessonGrassRepository repository = mock(LessonGrassRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-01-03T00:00:00Z"), ZoneId.of("Asia/Seoul"));
        GetLessonGrassService service = new GetLessonGrassService(repository, new LessonGrassLevelPolicy(4), new YearlyGrassPeriodPolicy(), clock);
        when(repository.findByMemberIdAndDateBetween(1L, LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-03")))
                .thenReturn(List.of(new LessonGrassStat(1L, LocalDate.parse("2026-01-02"), 3)));

        List<GetLessonGrassUseCase.LessonGrassView> original = service.handle(new GetLessonGrassQuery(1L, null));

        ObjectMapper plainObjectMapper = buildPlainObjectMapper();
        RedisSerializer<List<GetLessonGrassUseCase.LessonGrassView>> typedSerializer = new Jackson2JsonRedisSerializer<>(
                plainObjectMapper,
                (com.fasterxml.jackson.databind.JavaType) plainObjectMapper.getTypeFactory()
                        .constructCollectionType(List.class, GetLessonGrassUseCase.LessonGrassView.class)
        );

        byte[] serialized = typedSerializer.serialize(original);
        Object deserialized = typedSerializer.deserialize(serialized);

        assertThat(deserialized).isEqualTo(original);
    }

    private <T> void assertRoundTripsTyped(T original, Class<T> type) {
        Jackson2JsonRedisSerializer<T> typedSerializer = new Jackson2JsonRedisSerializer<>(buildPlainObjectMapper(), type);

        byte[] serialized = typedSerializer.serialize(original);
        T deserialized = typedSerializer.deserialize(serialized);

        assertThat(deserialized).isEqualTo(original);
    }

    private static ObjectMapper buildPlainObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    private void assertRoundTrips(Object original) {
        byte[] serialized = serializer.serialize(original);
        Object deserialized = serializer.deserialize(serialized);

        assertThat(deserialized).isEqualTo(original);
    }
}
