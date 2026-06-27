DELETE FROM video_progress;
DELETE FROM enrollment;
DELETE FROM lesson;
DELETE FROM course_section;
DELETE FROM course;

INSERT INTO course (course_id, title, thumbnail_url) VALUES (20, 'Spring Course', null);

INSERT INTO course_section (id, course_id, order_index) VALUES (30, 20, 0);

INSERT INTO lesson (id, section_id, order_index) VALUES (10, 30, 0);
INSERT INTO lesson (id, section_id, order_index) VALUES (11, 30, 1);

INSERT INTO video_progress (
    progress_id, member_id, course_id, video_id, last_position_sec, is_completed, updated_at
) VALUES (
    100, 1, 20, 10, 42, 1, CURRENT_TIMESTAMP
);

INSERT INTO enrollment (
    enrollment_id, member_id, course_id, enrolled_at, status, expired_at
) VALUES (
    200, 1, 20, CURRENT_TIMESTAMP, 'IN_PROGRESS', null
);
