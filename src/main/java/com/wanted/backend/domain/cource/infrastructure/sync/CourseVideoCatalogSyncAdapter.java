package com.wanted.backend.domain.cource.infrastructure.sync;

import com.wanted.backend.domain.cource.application.port.CourseVideoCatalogSyncPort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * course_section/lesson(작성 스키마)을 course_curriculum/video(재생 스키마)로 미러링한다.
 * learning_activity 엔티티를 직접 참조하지 않고 native SQL로 해당 테이블만 갱신(크로스 컨텍스트 규칙).
 *
 * 매핑은 PK 재사용: curriculum_id = course_section.id, video_id = lesson.id.
 * video.video_url(varchar 255)에는 길이가 긴 presigned URL을 넣지 않고 s3_key만 미러링한다.
 * 재생(S3VideoPlaybackUrlAdapter)은 s3_key로 presign하므로 충분하다.
 */
@Component
public class CourseVideoCatalogSyncAdapter implements CourseVideoCatalogSyncPort {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void syncByCourse(Long courseId) {
        upsertCurriculum(courseId);
        upsertVideo(courseId);
        deleteOrphanVideo(courseId);
        deleteOrphanCurriculum(courseId);
    }

    private void upsertCurriculum(Long courseId) {
        em.createNativeQuery("""
                INSERT INTO course_curriculum (curriculum_id, course_id, order_index)
                SELECT cs.id, cs.course_id, cs.order_index
                FROM course_section cs
                WHERE cs.course_id = :courseId
                ON DUPLICATE KEY UPDATE order_index = VALUES(order_index)
                """)
                .setParameter("courseId", courseId)
                .executeUpdate();
    }

    private void upsertVideo(Long courseId) {
        em.createNativeQuery("""
                INSERT INTO video (video_id, curriculum_id, s3_key, duration_seconds, sort_order, is_preview)
                SELECT l.id, l.section_id, l.s3_key, l.duration_seconds, l.order_index,
                       (cs.order_index = (SELECT MIN(cs2.order_index) FROM course_section cs2 WHERE cs2.course_id = :courseId)
                        AND l.order_index = (SELECT MIN(l2.order_index) FROM lesson l2 WHERE l2.section_id = cs.id))
                FROM lesson l
                JOIN course_section cs ON l.section_id = cs.id
                WHERE cs.course_id = :courseId
                ON DUPLICATE KEY UPDATE
                    curriculum_id    = VALUES(curriculum_id),
                    s3_key           = VALUES(s3_key),
                    duration_seconds = VALUES(duration_seconds),
                    sort_order       = VALUES(sort_order),
                    is_preview       = VALUES(is_preview)
                """)
                .setParameter("courseId", courseId)
                .executeUpdate();
    }

    // 강의에서 제거된 레슨에 대응하는 video 제거 (course_curriculum 삭제보다 먼저)
    private void deleteOrphanVideo(Long courseId) {
        em.createNativeQuery("""
                DELETE v FROM video v
                JOIN course_curriculum cc ON v.curriculum_id = cc.curriculum_id
                WHERE cc.course_id = :courseId
                  AND v.video_id NOT IN (
                      SELECT l.id FROM lesson l
                      JOIN course_section cs ON l.section_id = cs.id
                      WHERE cs.course_id = :courseId
                  )
                """)
                .setParameter("courseId", courseId)
                .executeUpdate();
    }

    // 강의에서 제거된 섹션에 대응하는 course_curriculum 제거
    private void deleteOrphanCurriculum(Long courseId) {
        em.createNativeQuery("""
                DELETE FROM course_curriculum
                WHERE course_id = :courseId
                  AND curriculum_id NOT IN (SELECT id FROM course_section WHERE course_id = :courseId)
                """)
                .setParameter("courseId", courseId)
                .executeUpdate();
    }
}
