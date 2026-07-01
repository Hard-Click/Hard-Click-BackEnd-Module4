#!/usr/bin/env bash
# k6 부하테스트로 병목(N+1, JOIN, 페이지네이션)을 재현하기 위한 로컬 더미데이터 시드 스크립트.
#
# 실행 위치: 아무 곳이든 무방 (docker exec로 hard-click-mysql 컨테이너에 직접 접속)
# 실행 방법: bash scripts/seed-load-test-data.sh
#
# 시드 대상과 규모를 이렇게 정한 이유 (코드 분석 결과 기준):
#   - course/reviews: CourseQueryService.getList()가 페이지 아이템마다 avgRating/reviewCount/
#     enrollmentCount를 개별 쿼리로 호출하는 N+1 구조. course 200개 + course당 리뷰 20~50개로
#     이 N+1이 실제로 느려지는 걸 재현한다.
#   - enrollment/payments: 이미 findByIdIn 등 배치 쿼리로 최적화돼 있어 N+1은 없지만,
#     순수 처리량/페이지네이션 비교용으로 적당량만 넣는다.
#   - subscription: 독립 컨트롤러가 없는 미완성 기능이라 시드하지 않는다.

set -euo pipefail

DB_SCHEMA="Hard-Click"
DB_PASSWORD="${DB_PASSWORD:-Hard-Click}"

mysql_exec() {
    docker exec -i hard-click-mysql mysql --default-character-set=utf8mb4 -u"Hard-Click" -p"$DB_PASSWORD" -D "$DB_SCHEMA" "$@"
}

echo "[1/8] 기존 데이터 정리 중..."
mysql_exec <<'SQL'
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE payments;
TRUNCATE TABLE order_items;
TRUNCATE TABLE orders;
TRUNCATE TABLE enrollment;
TRUNCATE TABLE reviews;
TRUNCATE TABLE lesson;
TRUNCATE TABLE course_section;
TRUNCATE TABLE course;
TRUNCATE TABLE members;
SET FOREIGN_KEY_CHECKS = 1;
SQL

echo "[2/8] demo_student 계정 생성 중..."
mysql_exec <<'SQL'
-- 비밀번호: Test1234! (BCrypt, k6 스크립트와 동일한 계정)
INSERT INTO members (member_id, username, email, password, name, role, status, is_locked, login_fail_count, is_password_change_required, optional_terms_agreed, created_at)
VALUES (1, 'demo_student', 'demo_student@example.com', '$2a$10$Mj7zTP8NoldPV1EA/PCFneIEhGtlTZzw6wNxQJnapSKcANycFkfou', '데모학생', 'STUDENT', 'ACTIVE', 0, 0, 0, 0, NOW());
SQL

echo "[3/8] 강사 20명 / 학생 480명 생성 중..."
mysql_exec <<'SQL'
DROP PROCEDURE IF EXISTS seed_instructors;
DELIMITER $$
CREATE PROCEDURE seed_instructors()
BEGIN
    DECLARE i INT DEFAULT 1;
    WHILE i <= 20 DO
        INSERT INTO members (username, email, password, name, role, status, is_locked, login_fail_count, is_password_change_required, optional_terms_agreed, created_at, one_line_intro)
        VALUES (
            CONCAT('instructor', i),
            CONCAT('instructor', i, '@example.com'),
            '$2a$10$Mj7zTP8NoldPV1EA/PCFneIEhGtlTZzw6wNxQJnapSKcANycFkfou',
            CONCAT('강사', i, '호'),
            'INSTRUCTOR', 'ACTIVE', 0, 0, 0, 0, NOW(),
            '실무 경험을 바탕으로 강의합니다'
        );
        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;
CALL seed_instructors();
DROP PROCEDURE seed_instructors;

DROP PROCEDURE IF EXISTS seed_students;
DELIMITER $$
CREATE PROCEDURE seed_students()
BEGIN
    DECLARE i INT DEFAULT 1;
    WHILE i <= 480 DO
        INSERT INTO members (username, email, password, name, role, status, is_locked, login_fail_count, is_password_change_required, optional_terms_agreed, created_at)
        VALUES (
            CONCAT('student', i),
            CONCAT('student', i, '@example.com'),
            '$2a$10$Mj7zTP8NoldPV1EA/PCFneIEhGtlTZzw6wNxQJnapSKcANycFkfou',
            CONCAT('학생', i),
            'STUDENT', 'ACTIVE', 0, 0, 0, 0, NOW()
        );
        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;
CALL seed_students();
DROP PROCEDURE seed_students;
SQL

echo "[4/8] 강의 200개 생성 중 (status=PUBLISHED, 목록 N+1 재현용)..."
mysql_exec <<'SQL'
DROP PROCEDURE IF EXISTS seed_courses;
DELIMITER $$
CREATE PROCEDURE seed_courses()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE instructor_id BIGINT;
    DECLARE subj VARCHAR(50);
    WHILE i <= 200 DO
        SET instructor_id = 2 + (i % 20);
        SET subj = ELT(1 + (i % 10), '수학', '영어', '국어', '물리학', '화학', '생명과학', '한국사', '지구과학', '컴퓨터공학', '중국어');
        INSERT INTO course (author_id, price, subject, title, description, level, price_type, status, created_at)
        VALUES (
            instructor_id,
            CASE WHEN i % 5 = 0 THEN 0 ELSE 10000 + (i % 10) * 5000 END,
            subj,
            CONCAT(subj, ' 강의 ', i, '편'),
            CONCAT(subj, ' 기초부터 심화까지 다루는 강의입니다. (', i, ')'),
            ELT(1 + (i % 3), '초급', '중급', '고급'),
            CASE WHEN i % 5 = 0 THEN 'FREE' ELSE 'PAID' END,
            'PUBLISHED',
            DATE_SUB(NOW(), INTERVAL (i % 90) DAY)
        );
        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;
CALL seed_courses();
DROP PROCEDURE seed_courses;
SQL

echo "[5/8] 강의별 섹션(3~5개)/레슨(4~7개) 생성 중..."
mysql_exec <<'SQL'
DROP PROCEDURE IF EXISTS seed_sections_lessons;
DELIMITER $$
CREATE PROCEDURE seed_sections_lessons()
BEGIN
    DECLARE cid BIGINT;
    DECLARE done INT DEFAULT 0;
    DECLARE n INT;
    DECLARE j INT;
    DECLARE sid BIGINT;
    DECLARE m INT;
    DECLARE k INT;
    DECLARE cur CURSOR FOR SELECT course_id FROM course;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    OPEN cur;
    course_loop: LOOP
        FETCH cur INTO cid;
        IF done THEN LEAVE course_loop; END IF;

        SET n = 3 + (cid % 3);
        SET j = 1;
        WHILE j <= n DO
            INSERT INTO course_section (course_id, order_index, title)
            VALUES (cid, j, CONCAT(j, '주차'));
            SET sid = LAST_INSERT_ID();

            SET m = 4 + (sid % 4);
            SET k = 1;
            WHILE k <= m DO
                INSERT INTO lesson (section_id, order_index, title, duration_seconds, created_at, file_processing_status)
                VALUES (sid, k, CONCAT(k, '강'), 600 + (k * 60), NOW(), 'COMPLETED');
                SET k = k + 1;
            END WHILE;

            SET j = j + 1;
        END WHILE;
    END LOOP;
    CLOSE cur;
END$$
DELIMITER ;
CALL seed_sections_lessons();
DROP PROCEDURE seed_sections_lessons;
SQL

echo "[6/8] 강의별 리뷰 20~50개 생성 중 (목록 N+1 재현용, 시간 좀 걸림)..."
mysql_exec <<'SQL'
DROP PROCEDURE IF EXISTS seed_reviews;
DELIMITER $$
CREATE PROCEDURE seed_reviews()
BEGIN
    DECLARE cid BIGINT;
    DECLARE done INT DEFAULT 0;
    DECLARE n INT;
    DECLARE j INT;
    DECLARE mid BIGINT;
    DECLARE cur CURSOR FOR SELECT course_id FROM course;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    OPEN cur;
    review_loop: LOOP
        FETCH cur INTO cid;
        IF done THEN LEAVE review_loop; END IF;

        SET n = 20 + (cid % 31);
        SET j = 1;
        WHILE j <= n DO
            SET mid = 22 + FLOOR(RAND() * 480);
            INSERT INTO reviews (course_id, member_id, rating, content, created_at, updated_at)
            VALUES (
                cid, mid, 1 + FLOOR(RAND() * 5),
                CONCAT('강의 후기 ', j, '번째입니다. 실전에 도움이 많이 됐어요.'),
                DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 60) DAY), NOW()
            );
            SET j = j + 1;
        END WHILE;
    END LOOP;
    CLOSE cur;
END$$
DELIMITER ;
CALL seed_reviews();
DROP PROCEDURE seed_reviews;
SQL

echo "[7/8] 수강 등록(enrollment) 약 2500건 생성 중..."
mysql_exec <<'SQL'
DROP PROCEDURE IF EXISTS seed_enrollments;
DELIMITER $$
CREATE PROCEDURE seed_enrollments()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE mid BIGINT;
    DECLARE cid BIGINT;
    WHILE i <= 3500 DO
        SET mid = 22 + FLOOR(RAND() * 480);
        SET cid = 1 + FLOOR(RAND() * 200);
        INSERT IGNORE INTO enrollment (member_id, course_id, enrolled_at, status)
        VALUES (mid, cid, DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 60) DAY), 'ENROLLED');
        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;
CALL seed_enrollments();
DROP PROCEDURE seed_enrollments;
SQL

echo "[8/8] 주문/결제 1200건 생성 중..."
mysql_exec <<'SQL'
DROP PROCEDURE IF EXISTS seed_orders_payments;
DELIMITER $$
CREATE PROCEDURE seed_orders_payments()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE mid BIGINT;
    DECLARE cid BIGINT;
    WHILE i <= 1200 DO
        SET mid = 22 + FLOOR(RAND() * 480);
        SET cid = 1 + FLOOR(RAND() * 200);

        INSERT INTO orders (order_id, member_id, order_no, payment_type)
        VALUES (i, mid, CONCAT('HC-LOAD-', LPAD(i, 6, '0')), 'COURSE');

        INSERT INTO order_items (order_item_id, order_id, course_id)
        VALUES (i, i, cid);

        INSERT INTO payments (payment_id, order_id, member_id, course_id, paid_amount, status, idempotency_key, pg_transaction_id, paid_at)
        VALUES (
            i, i, mid, cid,
            10000 + (i % 10) * 5000,
            CASE WHEN i % 20 = 0 THEN 'REFUNDED' WHEN i % 30 = 0 THEN 'FAILED' ELSE 'PAID' END,
            CONCAT('load-test-pay-', LPAD(i, 6, '0')),
            CONCAT('pg-load-', LPAD(i, 6, '0')),
            DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 60) DAY)
        );

        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;
CALL seed_orders_payments();
DROP PROCEDURE seed_orders_payments;
SQL

echo ""
echo "=== 완료 ==="
mysql_exec -N -B <<'SQL'
SELECT CONCAT('members: ', COUNT(*)) FROM members
UNION SELECT CONCAT('course: ', COUNT(*)) FROM course
UNION SELECT CONCAT('course_section: ', COUNT(*)) FROM course_section
UNION SELECT CONCAT('lesson: ', COUNT(*)) FROM lesson
UNION SELECT CONCAT('reviews: ', COUNT(*)) FROM reviews
UNION SELECT CONCAT('enrollment: ', COUNT(*)) FROM enrollment
UNION SELECT CONCAT('orders: ', COUNT(*)) FROM orders
UNION SELECT CONCAT('payments: ', COUNT(*)) FROM payments;
SQL
