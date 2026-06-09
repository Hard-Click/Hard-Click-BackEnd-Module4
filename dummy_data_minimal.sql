-- ================================================================
--  Hard-Click 시연용 최소 더미 데이터 (members only)
--  작성일: 2026-06-01
-- ================================================================
-- [계정 정보]
--   시연용 강사: demo_instructor / Test1234!  (member_id=1)
--   시연용 학생: demo_student    / Test1234!  (member_id=2)
-- ================================================================

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE video_progress;
TRUNCATE TABLE comments;
TRUNCATE TABLE reviews;
TRUNCATE TABLE posts;
TRUNCATE TABLE enrollment;
TRUNCATE TABLE lesson;
TRUNCATE TABLE course_section;
TRUNCATE TABLE course;
TRUNCATE TABLE subjects;
TRUNCATE TABLE members;

SET @pwd = '$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm';

-- ================================================================
-- 1. SUBJECTS (강의 등록 시 과목 선택에 필요)
-- ================================================================
INSERT INTO subjects (id, name) VALUES
(1, '국어'),
(2, '수학'),
(3, '영어'),
(4, '사회탐구'),
(5, '과학탐구'),
(6, '한국사'),
(7, '제2외국어');

-- ================================================================
-- 2. MEMBERS
-- ================================================================
INSERT INTO members
    (member_id, username, email, password, name, gender, birth_date, phone_number,
     profile_image_url, role, status, is_password_change_required, login_fail_count,
     is_locked, locked_at, last_login_at, created_at, updated_at, optional_terms_agreed)
VALUES
(1, 'demo_instructor', 'demo.instructor@test.com', @pwd, '시연강사', 'MALE',   '1985-03-15', '010-1111-0001', NULL, 'INSTRUCTOR', 'ACTIVE', 0, 0, 0, NULL, NOW(), NOW(), NOW(), 1),
(2, 'demo_student',   'demo.student@test.com',    @pwd, '시연학생', 'MALE',   '2000-01-15', '010-2222-0001', NULL, 'STUDENT',    'ACTIVE', 0, 0, 0, NULL, NOW(), NOW(), NOW(), 1);

ALTER TABLE members  AUTO_INCREMENT = 3;
ALTER TABLE subjects AUTO_INCREMENT = 8;

SET FOREIGN_KEY_CHECKS = 1;

-- ================================================================
-- 완료!
-- 시연용 강사: demo_instructor / Test1234!
-- 시연용 학생: demo_student    / Test1234!
-- ================================================================
