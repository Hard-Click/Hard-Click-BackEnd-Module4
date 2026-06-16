DELETE FROM video_progress;
DELETE FROM study_timer_sessions;
DELETE FROM enrollments;
DELETE FROM subscriptions;
DELETE FROM video;
DELETE FROM course_curriculum;
DELETE FROM courses;

INSERT INTO courses (
    course_id, instructor_id, subject_id, title, description, price,
    thumbnail_url, file_url, file_status, status, avg_rating, review_count,
    student_count, created_at, updated_at
) VALUES (
    20, 2, 3, 'Spring Course', null, 10000,
    null, null, null, 'PUBLISHED', 0.0, 0,
    0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

INSERT INTO course_curriculum (
    curriculum_id, course_id, title, duration_minutes, order_index
) VALUES (
    30, 20, 'Section 1', 60, 1
);

INSERT INTO video (
    video_id, curriculum_id, title, video_url, is_preview, duration_seconds, sort_order
) VALUES (
    10, 30, 'Video 1', 'https://stream.example.com/video.m3u8', 1, 300, 1
);

INSERT INTO video_progress (
    progress_id, member_id, course_id, video_id, last_position_sec,
    watch_time_sec, is_completed, completed_at, updated_at
) VALUES (
    100, 1, 20, 10, 42,
    120, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

INSERT INTO enrollments (
    enrollment_id, member_id, course_id, payment_type, status,
    progress_rate, expired_at, created_at, updated_at
) VALUES (
    200, 1, 20, 'COURSE', 'IN_PROGRESS',
    10.0, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

INSERT INTO subscriptions (
    subscription_id, member_id, plan_id, payment_method, paid_amount,
    status, started_at, expired_at, cancelled_at, created_at
) VALUES (
    300, 1, 1, 'CARD', 9900,
    'ACTIVE', CURRENT_TIMESTAMP, DATEADD('DAY', 10, CURRENT_TIMESTAMP), null, CURRENT_TIMESTAMP
);
