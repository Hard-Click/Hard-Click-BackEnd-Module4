package com.wanted.backend.domain.admin_dashboard.infrastructure.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wanted.backend.domain.admin_dashboard.application.dto.AdminDashboardResult;
import com.wanted.backend.domain.community.domain.model.TargetType;
import com.wanted.backend.domain.community.infrastructure.persistence.ReportJpaEntity;
import com.wanted.backend.domain.notice.domain.model.NoticeStatus;
import com.wanted.backend.domain.notice.infrastructure.persistence.NoticeJpaEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RedisConfig와 동일한 캐시 직렬화 설정으로, 실제 AdminDashboardQueryAdapter(JPA 쿼리 포함)가
 * 만들어낸 AdminDashboardResult가 라운드트립에 성공하는지 검증한다.
 * AdminDashboardResult를 직접 new로 만들면 findRecentReports()/findRecentNotices()의
 * ArrayList 수집 로직이 회귀해도 테스트가 못 잡기 때문에, 어댑터를 실제로 거친다.
 */
@DataJpaTest(properties = {
        "spring.jpa.database=H2",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@ActiveProfiles("test")
class AdminDashboardQueryAdapterCacheSerializationTest {

    @Autowired
    private TestEntityManager em;

    @Test
    void roundTripsAdminDashboardResultProducedByTheRealAdapter() {
        LocalDateTime now = LocalDateTime.parse("2026-06-01T10:00:00");
        em.persist(new ReportJpaEntity(1L, 10L, TargetType.COMMENT, 1L, "SPAM", "광고성", now));
        em.persist(new ReportJpaEntity(2L, 10L, TargetType.COMMENT, 1L, "SPAM", "광고성", now.plusMinutes(1)));
        em.persist(new ReportJpaEntity(3L, 10L, TargetType.COMMENT, 1L, "SPAM", "광고성", now.plusMinutes(2)));
        em.persist(new NoticeJpaEntity(
                1L, null, "공지 제목", "공지 내용", true, "GLOBAL", NoticeStatus.PUBLISHED, now, now
        ));
        em.flush();

        AdminDashboardQueryAdapter adapter = new AdminDashboardQueryAdapter(em.getEntityManager());
        AdminDashboardResult original = adapter.findDashboard();

        assertThat(original.recentReports()).hasSize(1);
        assertThat(original.recentNotices()).hasSize(1);

        GenericJackson2JsonRedisSerializer serializer = buildCacheSerializer();
        byte[] serialized = serializer.serialize(original);
        Object deserialized = serializer.deserialize(serialized);

        assertThat(deserialized).isEqualTo(original);
    }

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
}
