package com.wanted.backend.domain.learning_activity.infrastructure;

import com.wanted.backend.domain.learning_activity.application.port.CourseProgressQueryPort;
import com.wanted.backend.domain.learning_activity.domain.model.VideoAccessInfo;
import com.wanted.backend.domain.learning_activity.domain.model.VideoProgress;
import com.wanted.backend.domain.learning_activity.infrastructure.curriculum.CourseCurriculumReferenceEntity;
import com.wanted.backend.domain.learning_activity.infrastructure.curriculum.CourseCurriculumReferenceRepository;
import com.wanted.backend.domain.learning_activity.infrastructure.catalog.CatalogCourseReferenceEntity;
import com.wanted.backend.domain.learning_activity.infrastructure.catalog.CatalogCourseReferenceRepository;
import com.wanted.backend.domain.learning_activity.infrastructure.catalog.CatalogVideoReferenceEntity;
import com.wanted.backend.domain.learning_activity.infrastructure.catalog.CatalogVideoReferenceRepository;
import com.wanted.backend.domain.learning_activity.infrastructure.catalog.VideoCatalogAdapter;
import com.wanted.backend.domain.learning_activity.infrastructure.video.VideoReferenceEntity;
import com.wanted.backend.domain.learning_activity.infrastructure.video.VideoReferenceRepository;
import com.wanted.backend.domain.learning_activity.infrastructure.enrollment.EnrollmentAccessAdapter;
import com.wanted.backend.domain.learning_activity.infrastructure.enrollment.EnrollmentReferenceJpaEntity;
import com.wanted.backend.domain.learning_activity.infrastructure.enrollment.SpringDataEnrollmentAccessRepository;
import com.wanted.backend.domain.learning_activity.infrastructure.persistence.CourseProgressQueryAdapter;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@EntityScan(basePackageClasses = {
        VideoProgressJpaEntity.class,
        EnrollmentReferenceJpaEntity.class,
        SubscriptionReferenceJpaEntity.class,
        CourseCurriculumReferenceEntity.class,
        VideoReferenceEntity.class,
        CatalogVideoReferenceEntity.class,
        CatalogCourseReferenceEntity.class
})
@EnableJpaRepositories(basePackageClasses = {
        SpringDataVideoProgressRepository.class,
        CourseCurriculumReferenceRepository.class,
        VideoReferenceRepository.class,
        CatalogVideoReferenceRepository.class,
        CatalogCourseReferenceRepository.class,
        SpringDataEnrollmentAccessRepository.class,
        SpringDataSubscriptionAccessRepository.class
})
@Import({
        VideoCatalogAdapter.class,
        EnrollmentAccessAdapter.class,
        SubscriptionAccessAdapter.class,
        CourseProgressQueryAdapter.class,
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
    private CourseProgressQueryAdapter courseProgressQueryAdapter;

    @Autowired
    private EnrollmentAccessAdapter enrollmentAccessAdapter;

    @Autowired
    private SubscriptionAccessAdapter subscriptionAccessAdapter;

    @Test
    void 영상_카탈로그_어댑터가_영상_접근_정보를_조회한다() {
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
    void 영상_진도_저장소_어댑터가_진도_정보를_조회한다() {
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
    void 강의_진도_조회_어댑터가_강의_진도_정보를_조회한다() {
        CourseProgressQueryPort.CourseProgressData progress =
                courseProgressQueryAdapter.findByMemberIdAndCourseId(1L, 20L);

        assertThat(progress.courseId()).isEqualTo(20L);
        assertThat(progress.lessons()).hasSize(1);
        assertThat(progress.lessons().get(0).videoId()).isEqualTo(10L);
        assertThat(progress.lessons().get(0).completed()).isTrue();
        assertThat(progress.lessons().get(0).lastPositionSeconds()).isEqualTo(42);
    }

    @Test
    void 영상_진도_저장소_어댑터가_마지막_재생_위치를_저장한다() {
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
    void 영상_진도_저장소_어댑터가_시청_시간을_저장한다() {
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
    void 영상_진도_저장소_어댑터가_완료_상태를_저장한다() {
        VideoProgress progress = videoProgressRepositoryAdapter.findByMemberIdAndVideoId(1L, 10L)
                .orElseThrow()
                .complete(LocalDateTime.now());

        VideoProgress saved = videoProgressRepositoryAdapter.save(progress);

        assertThat(saved.id()).isEqualTo(100L);
        assertThat(saved.completed()).isTrue();
        assertThat(saved.completedAt()).isNotNull();
    }

    @Test
    void 수강권_접근_어댑터가_활성_수강권을_확인한다() {
        boolean result = enrollmentAccessAdapter.hasActiveEnrollment(1L, 20L);

        assertThat(result).isTrue();
    }

    @Test
    void 구독권_접근_어댑터가_활성_구독권을_확인한다() {
        boolean result = subscriptionAccessAdapter.hasActiveSubscription(1L);

        assertThat(result).isTrue();
    }
}
