#!/usr/bin/env bash
# seed-load-test-data.sh 로 채운 course/members/reviews/enrollment/payments 위에,
# "모든 도메인"을 기준으로 부하테스트 대상에 넣기 위해 나머지 도메인 데이터를 추가한다.
#
# 전제: scripts/seed-load-test-data.sh 를 먼저 실행해서 members/course가 있어야 함.
# 실행 방법: bash scripts/seed-load-test-data-domains.sh
#
# 의도적으로 시드하지 않는 것:
#   - video / video_progress / course_curriculum (learning_activity 도메인)
#     -> 코드 확인 결과 이 도메인은 실제 강의 카탈로그(course/course_section/lesson)와
#        연결된 게 아니라 별도의 레거시 스키마(courses(복수형)/course_curriculum/video)를
#        보고 있음. 실제 서비스 플로우와 무관한 가짜 데이터를 넣는 건 의미가 없어서
#        스킵하고, 이 구조적 문제 자체를 별도로 보고한다.
#   - subscription 관련 테이블 -> 독립 기능 없음(이미 확인됨).

set -euo pipefail

DB_SCHEMA="Hard-Click"
DB_PASSWORD="${DB_PASSWORD:-Hard-Click}"

mysql_exec() {
    docker exec -i hard-click-mysql mysql --default-character-set=utf8mb4 -u"Hard-Click" -p"$DB_PASSWORD" -D "$DB_SCHEMA" "$@"
}

echo "[1/4] subjects(과목) 10개 생성 중..."
mysql_exec <<'SQL'
INSERT INTO subjects (name) VALUES
('수학'), ('영어'), ('국어'), ('물리학'), ('화학'),
('생명과학'), ('한국사'), ('지구과학'), ('컴퓨터공학'), ('중국어');
SQL

echo "[2/4] notices(공지) 생성 중 (GLOBAL 30개 + 강의별 COURSE 공지)..."
mysql_exec <<'SQL'
DROP PROCEDURE IF EXISTS seed_global_notices;
DELIMITER $$
CREATE PROCEDURE seed_global_notices()
BEGIN
    DECLARE i INT DEFAULT 1;
    WHILE i <= 30 DO
        INSERT INTO notices (author_id, course_id, type, title, content, is_pinned, status, created_at, updated_at)
        VALUES (1, NULL, 'GLOBAL', CONCAT('전체 공지 ', i, '번'), CONCAT('전체 공지사항 내용입니다. (', i, ')'), IF(i <= 3, 1, 0), 'PUBLISHED', DATE_SUB(NOW(), INTERVAL i DAY), NOW());
        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;
CALL seed_global_notices();
DROP PROCEDURE seed_global_notices;

DROP PROCEDURE IF EXISTS seed_course_notices;
DELIMITER $$
CREATE PROCEDURE seed_course_notices()
BEGIN
    DECLARE cid BIGINT;
    DECLARE done INT DEFAULT 0;
    DECLARE n INT;
    DECLARE j INT;
    DECLARE author BIGINT;
    DECLARE cur CURSOR FOR SELECT course_id, author_id FROM course;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    OPEN cur;
    notice_loop: LOOP
        FETCH cur INTO cid, author;
        IF done THEN LEAVE notice_loop; END IF;

        SET n = 1 + (cid % 3);
        SET j = 1;
        WHILE j <= n DO
            INSERT INTO notices (author_id, course_id, type, title, content, is_pinned, status, created_at, updated_at)
            VALUES (author, cid, 'COURSE', CONCAT('강의 공지 ', j, '번'), CONCAT('수강생 안내사항입니다. (', j, ')'), 0, 'PUBLISHED', DATE_SUB(NOW(), INTERVAL j DAY), NOW());
            SET j = j + 1;
        END WHILE;
    END LOOP;
    CLOSE cur;
END$$
DELIMITER ;
CALL seed_course_notices();
DROP PROCEDURE seed_course_notices;
SQL

echo "[3/4] posts(게시글) 2000개 생성 중 (FREE/QUESTION)..."
mysql_exec <<'SQL'
DROP PROCEDURE IF EXISTS seed_posts;
DELIMITER $$
CREATE PROCEDURE seed_posts()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE mid BIGINT;
    DECLARE bt VARCHAR(10);
    WHILE i <= 2000 DO
        SET mid = 1 + FLOOR(RAND() * 501);
        SET bt = IF(i % 2 = 0, 'FREE', 'QUESTION');
        INSERT INTO posts (author_id, board_type, title, content, view_count, status, is_accepted, subject_id, created_at, updated_at)
        VALUES (
            mid, bt,
            CONCAT(IF(bt = 'QUESTION', '질문: ', '잡담: '), i, '번째 게시글'),
            CONCAT('게시글 내용입니다. 부하테스트용 더미데이터입니다. (', i, ')'),
            FLOOR(RAND() * 500),
            'ACTIVE',
            IF(bt = 'QUESTION' AND i % 7 = 0, 1, 0),
            1 + (i % 10),
            DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 90) DAY),
            NOW()
        );
        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;
CALL seed_posts();
DROP PROCEDURE seed_posts;
SQL

echo "[4/4] comments(댓글) 약 8000개 생성 중 (게시글당 0~10개, 일부는 답글)..."
mysql_exec <<'SQL'
DROP PROCEDURE IF EXISTS seed_comments;
DELIMITER $$
CREATE PROCEDURE seed_comments()
BEGIN
    DECLARE pid BIGINT;
    DECLARE done INT DEFAULT 0;
    DECLARE n INT;
    DECLARE j INT;
    DECLARE mid BIGINT;
    DECLARE first_comment_id BIGINT;
    DECLARE cur CURSOR FOR SELECT post_id FROM posts;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    OPEN cur;
    comment_loop: LOOP
        FETCH cur INTO pid;
        IF done THEN LEAVE comment_loop; END IF;

        SET n = FLOOR(RAND() * 11);
        SET j = 1;
        SET first_comment_id = NULL;
        WHILE j <= n DO
            SET mid = 1 + FLOOR(RAND() * 501);
            INSERT INTO comments (post_id, author_id, parent_id, content, is_accepted, is_deleted, accept_count, created_at, updated_at)
            VALUES (
                pid, mid,
                IF(j > 1 AND RAND() < 0.3, first_comment_id, NULL),
                CONCAT('댓글 ', j, '번째입니다.'),
                IF(j = 1 AND RAND() < 0.1, 1, 0),
                0,
                0,
                DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 60) DAY),
                NOW()
            );
            IF j = 1 THEN
                SET first_comment_id = LAST_INSERT_ID();
            END IF;
            SET j = j + 1;
        END WHILE;
    END LOOP;
    CLOSE cur;
END$$
DELIMITER ;
CALL seed_comments();
DROP PROCEDURE seed_comments;
SQL

echo ""
echo "=== 완료 ==="
mysql_exec -N -B <<'SQL'
SELECT CONCAT('subjects: ', COUNT(*)) FROM subjects
UNION SELECT CONCAT('notices: ', COUNT(*)) FROM notices
UNION SELECT CONCAT('posts: ', COUNT(*)) FROM posts
UNION SELECT CONCAT('comments: ', COUNT(*)) FROM comments;
SQL
