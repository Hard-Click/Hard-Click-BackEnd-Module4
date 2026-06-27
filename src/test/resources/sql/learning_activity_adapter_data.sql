DELETE FROM video_progress;
DELETE FROM study_timer_sessions;
DELETE FROM enrollment;
DELETE FROM subscriptions;
DELETE FROM lesson;
DELETE FROM course_section;
DELETE FROM course;

INSERT INTO course (
    course_id, instructor_id, subject_id, title, description, price,
    thumbnail_url, file_url, file_status, status, avg_rating, review_count,
    student_count, created_at, updated_at
) VALUES (
    20, 2, 3, 'Spring Course', null, 10000,
    null, null, null, 'PUBLISHED', 0.0, 0,
    0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

INSERT INTO course_section (
    id, course_id, title, order_index
) VALUES (
    30, 20, 'Section 1', 0
);

INSERT INTO lesson (
    id, section_id, title, order_index, video_url, s3_key, duration_seconds, created_at
) VALUES (
    10, 30, 'Lesson 1', 0, 'https://stream.example.com/video.m3u8', 'videos/10.mp4', 300, CURRENT_TIMESTAMP
);

INSERT INTO video_progress (
    progress_id, member_id, course_id, video_id, last_position_sec,
    watch_time_sec, is_completed, completed_at, updated_at
) VALUES (
    100, 1, 20, 10, 42,
    120, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

INSERT INTO enrollment (
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
