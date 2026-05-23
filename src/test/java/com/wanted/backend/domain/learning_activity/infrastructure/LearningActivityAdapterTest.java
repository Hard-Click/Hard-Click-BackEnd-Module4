package com.wanted.backend.domain.learning_activity.infrastructure;

import com.wanted.backend.domain.learning_activity.domain.model.VideoAccessInfo;
import com.wanted.backend.domain.learning_activity.domain.model.VideoProgress;
import com.wanted.backend.domain.learning_activity.infrastructure.catalog.VideoCatalogAdapter;
import com.wanted.backend.domain.learning_activity.infrastructure.catalog.SpringDataVideoCatalogRepository;
import com.wanted.backend.domain.learning_activity.infrastructure.enrollment.EnrollmentAccessAdapter;
import com.wanted.backend.domain.learning_activity.infrastructure.enrollment.EnrollmentReferenceJpaEntity;
import com.wanted.backend.domain.learning_activity.infrastructure.enrollment.SpringDataEnrollmentAccessRepository;
import com.wanted.backend.domain.learning_activity.infrastructure.persistence.VideoProgressRepositoryAdapter;
import com.wanted.backend.domain.learning_activity.infrastructure.persistence.VideoProgressJpaEntity;
import com.wanted.backend.domain.learning_activity.infrastructure.persistence.SpringDataVideoProgressRepository;
import com.wanted.backend.domain.learning_activity.infrastructure.subscription.SubscriptionAccessAdapter;
import com.wanted.backend.domain.learning_activity.infrastructure.subscription.SubscriptionReferenceJpaEntity;
import com.wanted.backend.domain.learning_activity.infrastructure.subscription.SpringDataSubscriptionAccessRepository;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@EntityScan(basePackageClasses = {
        VideoProgressJpaEntity.class,
        EnrollmentReferenceJpaEntity.class,
        SubscriptionReferenceJpaEntity.class
})
@EnableJpaRepositories(basePackageClasses = {
        SpringDataVideoProgressRepository.class,
        SpringDataVideoCatalogRepository.class,
        SpringDataEnrollmentAccessRepository.class,
        SpringDataSubscriptionAccessRepository.class
})
@Import({
        VideoCatalogAdapter.class,
        EnrollmentAccessAdapter.class,
        SubscriptionAccessAdapter.class,
        VideoProgressRepositoryAdapter.class
})
@Sql(scripts = {
        "/sql/learning_activity_adapter_schema.sql",
        "/sql/learning_activity_adapter_data.sql"
})
class LearningActivityAdapterTest {

    @Autowired
    private VideoCatalogAdapter videoCatalogAdapter;

    @Autowired
    private VideoProgressRepositoryAdapter videoProgressRepositoryAdapter;

    @Autowired
    private EnrollmentAccessAdapter enrollmentAccessAdapter;

    @Autowired
    private SubscriptionAccessAdapter subscriptionAccessAdapter;

    @Test
    void videoCatalogAdapterLoadsVideoAccessInfo() {
        Optional<VideoAccessInfo> result = videoCatalogAdapter.findByVideoId(10L);

        assertThat(result).isPresent();
        VideoAccessInfo accessInfo = result.get();
        assertThat(accessInfo.videoId()).isEqualTo(10L);
        assertThat(accessInfo.courseId()).isEqualTo(20L);
        assertThat(accessInfo.courseStatus()).isEqualTo("PUBLISHED");
        assertThat(accessInfo.coursePrice()).isEqualTo(10000);
        assertThat(accessInfo.preview()).isTrue();
        assertThat(accessInfo.streamingUrl()).isEqualTo("https://stream.example.com/video.m3u8");
        assertThat(accessInfo.durationSeconds()).isEqualTo(300);
    }

    @Test
    void videoProgressRepositoryAdapterLoadsProgress() {
        Optional<VideoProgress> result = videoProgressRepositoryAdapter.findByMemberIdAndVideoId(1L, 10L);

        assertThat(result).isPresent();
        VideoProgress progress = result.get();
        assertThat(progress.id()).isEqualTo(100L);
        assertThat(progress.memberId()).isEqualTo(1L);
        assertThat(progress.courseId()).isEqualTo(20L);
        assertThat(progress.videoId()).isEqualTo(10L);
        assertThat(progress.lastPositionSec()).isEqualTo(42);
        assertThat(progress.watchTimeSec()).isEqualTo(120);
        assertThat(progress.completed()).isTrue();
    }

    @Test
    void videoProgressRepositoryAdapterSavesLastPosition() {
        VideoProgress progress = VideoProgress.empty(1L, 20L, 10L)
                .updateLastPosition(142);

        VideoProgress saved = videoProgressRepositoryAdapter.save(progress);

        assertThat(saved.id()).isNotNull();
        assertThat(saved.memberId()).isEqualTo(1L);
        assertThat(saved.courseId()).isEqualTo(20L);
        assertThat(saved.videoId()).isEqualTo(10L);
        assertThat(saved.lastPositionSec()).isEqualTo(142);
        assertThat(saved.watchTimeSec()).isZero();
        assertThat(saved.completed()).isFalse();
    }

    @Test
    void videoProgressRepositoryAdapterSavesWatchTime() {
        VideoProgress progress = videoProgressRepositoryAdapter.findByMemberIdAndVideoId(1L, 10L)
                .orElseThrow()
                .addWatchTime(30);

        VideoProgress saved = videoProgressRepositoryAdapter.save(progress);

        assertThat(saved.id()).isEqualTo(100L);
        assertThat(saved.lastPositionSec()).isEqualTo(42);
        assertThat(saved.watchTimeSec()).isEqualTo(150);
        assertThat(saved.completed()).isTrue();
    }

    @Test
    void enrollmentAccessAdapterChecksActiveEnrollment() {
        boolean result = enrollmentAccessAdapter.hasActiveEnrollment(1L, 20L);

        assertThat(result).isTrue();
    }

    @Test
    void subscriptionAccessAdapterChecksActiveSubscription() {
        boolean result = subscriptionAccessAdapter.hasActiveSubscription(1L);

        assertThat(result).isTrue();
    }
}
