-- ================================================================
--  게시글 목록 조회 병목 벤치마크 (담당: 윤종호 — 방법 ① 인덱스 / ④ 비정규화)
--  실제 스키마(박종준 공유 DDL) 기준, 단일서버(2vCPU/2GB) 규모 축소판
--    members  5,000
--    posts   50,000
--    comments 500,000  (일부 게시글에 편중 분포 — 정렬 병목 재현용)
--
--  ⚠️ 실행 방법: 문자셋 깨짐 방지 위해 아래처럼 실행
--    mysql --default-character-set=utf8mb4 -h127.0.0.1 -u[계정] -p[비번] [DB명] < benchmark_post_list.sql
--
--  ⚠️ [E] 방법 ④ 비정규화는 ②③ 끝나기 전까지 실행하지 마세요 (윤종호가 마지막에 실행)
-- ================================================================

-- ================================================================
-- [A] 대량 더미 데이터 시딩
-- ================================================================
SET FOREIGN_KEY_CHECKS = 0;
SET cte_max_recursion_depth = 600000;

-- 이전 실행에서 남은 벤치마크 데이터 정리 (재실행해도 항상 깨끗하게 시작하도록)
DELETE FROM comments WHERE content LIKE '벤치마크 댓글 %';
DELETE FROM posts WHERE title LIKE '벤치마크 게시글 %';
DELETE FROM members WHERE username LIKE 'bench_user_%';

-- 시퀀스 생성용 임시테이블 (INSERT...WITH RECURSIVE 조합은 MySQL 8.0.19 이전 버전에서 문법 에러가 나서
-- 임시테이블로 분리 — CREATE TABLE...AS WITH RECURSIVE는 모든 8.0 버전에서 동작함)
DROP TEMPORARY TABLE IF EXISTS seq_5000;
CREATE TEMPORARY TABLE seq_5000 AS
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 5000
)
SELECT n FROM seq;

DROP TEMPORARY TABLE IF EXISTS seq_50000;
CREATE TEMPORARY TABLE seq_50000 AS
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 50000
)
SELECT n FROM seq;

DROP TEMPORARY TABLE IF EXISTS seq_500000;
CREATE TEMPORARY TABLE seq_500000 AS
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 500000
)
SELECT n FROM seq;

-- A-1. members 5,000건
INSERT INTO members
(username, email, password, name, gender, birth_date, phone_number,
 role, status, is_password_change_required, login_fail_count,
 is_locked, optional_terms_agreed, created_at, updated_at)
SELECT
    CONCAT('bench_user_', n),
    CONCAT('bench_user_', n, '@test.com'),
    '$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm', -- Test1234!
    CONCAT('벤치유저', n),
    IF(n % 2 = 0, 'MALE', 'FEMALE'),
    '1995-01-01',
    CONCAT('010-0000-', LPAD(n, 4, '0')),
    'STUDENT',
    'ACTIVE',
    0, 0, 0, 1,
    NOW(), NOW()
FROM seq_5000;

-- A-2. posts 50,000건 — board_type/status 분산, author는 위 members 중에서 순환 배정
INSERT INTO posts
(author_id, board_type, title, content, view_count, status, is_accepted, created_at, updated_at)
SELECT
    ((n - 1) % 5000) + (SELECT MIN(member_id) FROM members WHERE username LIKE 'bench_user_%'),
    IF(n % 2 = 0, 'FREE', 'QUESTION'),
    CONCAT('벤치마크 게시글 ', n),
    CONCAT('벤치마크용 본문 내용입니다. 게시글 번호 ', n),
    FLOOR(RAND() * 500),
    'ACTIVE',
    0,
    NOW() - INTERVAL (50000 - n) MINUTE,
    NOW() - INTERVAL (50000 - n) MINUTE
FROM seq_50000;

-- A-3. comments 500,000건 — 일부 게시글(상위 10%)에 댓글이 편중되게 분포
--      편중 없이 균등 분포만 만들면 "정렬 병목"이 과소평가되므로 인기글 패턴을 재현한다.
INSERT INTO comments
(post_id, author_id, parent_id, content, is_accepted, is_deleted, status, accept_count, created_at, updated_at)
SELECT
    CASE
        -- 70%의 댓글은 인기글 5,000건(전체 게시글의 상위 10%)에 집중
        WHEN n % 10 < 7 THEN (SELECT MIN(post_id) FROM posts) + (n % 5000)
        -- 나머지 30%는 전체 게시글에 균등 분포
        ELSE (SELECT MIN(post_id) FROM posts) + (n % 50000)
        END,
    (SELECT MIN(member_id) FROM members WHERE username LIKE 'bench_user_%') + (n % 5000),
    NULL,
    CONCAT('벤치마크 댓글 ', n),
    0, 0, 'ACTIVE', 0,
    NOW() - INTERVAL (500000 - n) SECOND,
    NOW() - INTERVAL (500000 - n) SECOND
FROM seq_500000;

DROP TEMPORARY TABLE IF EXISTS seq_5000, seq_50000, seq_500000;
SET FOREIGN_KEY_CHECKS = 1;
ANALYZE TABLE members, posts, comments;

-- ================================================================
-- [A-4] 대댓글(parent_id) 백필 — 전체 댓글의 약 20%를 대댓글로 변환
--       parent_id 인덱스/배치조회 테스트용
-- ================================================================
UPDATE comments c1
    JOIN (
        SELECT comment_id, post_id,
               LAG(comment_id) OVER (PARTITION BY post_id ORDER BY comment_id) AS prev_comment_id
        FROM comments
        WHERE content LIKE '벤치마크 댓글 %'
    ) ranked ON c1.comment_id = ranked.comment_id
SET c1.parent_id = ranked.prev_comment_id
WHERE c1.comment_id % 5 = 0
  AND ranked.prev_comment_id IS NOT NULL;


-- 댓글수 카운트 단건 패턴 (N+1 재현용)
EXPLAIN
SELECT COUNT(*) FROM comments WHERE post_id = (SELECT MIN(post_id) FROM posts);


-- ================================================================
-- [C] 방법 ① 인덱스 추가 (윤종호 — 이미 적용 완료된 상태로 넘김)
-- ================================================================
CREATE INDEX idx_comments_post_id        ON comments (post_id);
CREATE INDEX idx_comments_author_id      ON comments (author_id);
CREATE INDEX idx_comments_parent_id      ON comments (parent_id);   -- 대댓글 배치 조회 N+1 차단 (박종준)
CREATE INDEX idx_posts_board_status_created ON posts (board_type, status, created_at);
ANALYZE TABLE posts, comments;


-- ================================================================
-- [D] AFTER — 인덱스 적용 후 동일 쿼리 재측정 (type: ALL -> ref 확인)
-- ================================================================
EXPLAIN ANALYZE
SELECT p.post_id, p.title, p.author_id, p.created_at,
       (SELECT COUNT(*) FROM comments c WHERE c.post_id = p.post_id) AS comment_count
FROM posts p
WHERE p.board_type = 'FREE' AND p.status = 'ACTIVE'
ORDER BY (SELECT COUNT(*) FROM comments c WHERE c.post_id = p.post_id) DESC
LIMIT 20 OFFSET 0;

EXPLAIN ANALYZE
SELECT COUNT(*) FROM comments WHERE post_id = (SELECT MIN(post_id) FROM posts);


-- ================================================================
-- [E] 방법 ④ 비정규화 comment_count — ⚠️ ②③ 끝난 뒤 윤종호가 마지막에 실행
-- ================================================================
-- ALTER TABLE posts ADD COLUMN comment_count INT NOT NULL DEFAULT 0;
--
-- UPDATE posts p
-- SET p.comment_count = (
--     SELECT COUNT(*) FROM comments c WHERE c.post_id = p.post_id
-- );
--
-- CREATE INDEX idx_posts_board_status_count ON posts (board_type, status, comment_count);
-- ANALYZE TABLE posts;
--
-- EXPLAIN ANALYZE
-- SELECT p.post_id, p.title, p.author_id, p.created_at, p.comment_count
-- FROM posts p
-- WHERE p.board_type = 'FREE' AND p.status = 'ACTIVE'
-- ORDER BY p.comment_count DESC
-- LIMIT 20 OFFSET 0;
--
-- SELECT p.post_id, p.comment_count AS denormalized,
--        (SELECT COUNT(*) FROM comments c WHERE c.post_id = p.post_id) AS actual
-- FROM posts p
-- ORDER BY p.post_id
-- LIMIT 20;


-- ================================================================
-- [F] 댓글 상세 조회 N+1 벤치마크 (담당: 박종준 — Batch IN + Map)
--  위 [A]에서 만든 데이터셋은 post_id/author_id가 둘 다 (n % 5000)에서 파생돼
--  같은 글 안에서 작성자가 거의 겹치지 않아(한 글 = 거의 한 작성자) 댓글 상세
--  화면의 N+1(작성자 다양성 + 대댓글)을 보여주기엔 부적합하다.
--  같은 members/posts 풀을 그대로 재사용해, 댓글 상세 조회에서 N+1이 터지는
--  핫포스트 1건만 별도로 만든다.
-- ================================================================
SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM comments WHERE post_id = (SELECT post_id FROM posts WHERE title = '박종준 벤치마크 핫포스트' LIMIT 1);
DELETE FROM posts WHERE title = '박종준 벤치마크 핫포스트';

INSERT INTO posts
(author_id, board_type, title, content, view_count, status, is_accepted, created_at, updated_at)
VALUES (
    (SELECT MIN(member_id) FROM members WHERE username LIKE 'bench_user_%'),
    'FREE', '박종준 벤치마크 핫포스트', '댓글 상세 N+1 측정용 핫포스트', 99999, 'ACTIVE', 0,
    NOW(), NOW()
);

DROP TEMPORARY TABLE IF EXISTS seq_500;
CREATE TEMPORARY TABLE seq_500 AS
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 500
)
SELECT n FROM seq;

-- F-1. 부모 댓글 500건 — 작성자를 매번 다른 사람으로 배정해 이름 배치조회 N+1 재현
INSERT INTO comments
(post_id, author_id, parent_id, content, is_accepted, is_deleted, status, accept_count, created_at, updated_at)
SELECT
    (SELECT post_id FROM posts WHERE title = '박종준 벤치마크 핫포스트' LIMIT 1),
    (SELECT MIN(member_id) FROM members WHERE username LIKE 'bench_user_%') + (n % 5000),
    NULL,
    CONCAT('박종준 벤치마크 댓글 ', n),
    0, 0, 'ACTIVE', 0,
    NOW() - INTERVAL (500 - n) SECOND,
    NOW() - INTERVAL (500 - n) SECOND
FROM seq_500;

-- F-2. 대댓글 2,000건 — 부모 댓글마다 평균 4개씩, 작성자도 매번 다르게 배정
DROP TEMPORARY TABLE IF EXISTS seq_2000;
CREATE TEMPORARY TABLE seq_2000 AS
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 2000
)
SELECT n FROM seq;

INSERT INTO comments
(post_id, author_id, parent_id, content, is_accepted, is_deleted, status, accept_count, created_at, updated_at)
SELECT
    (SELECT post_id FROM posts WHERE title = '박종준 벤치마크 핫포스트' LIMIT 1),
    (SELECT MIN(member_id) FROM members WHERE username LIKE 'bench_user_%') + ((n * 7) % 5000),
    (SELECT MIN(comment_id) FROM comments WHERE content LIKE '박종준 벤치마크 댓글 %') + (n % 500),
    CONCAT('박종준 벤치마크 대댓글 ', n),
    0, 0, 'ACTIVE', 0,
    NOW() - INTERVAL (2000 - n) SECOND,
    NOW() - INTERVAL (2000 - n) SECOND
FROM seq_2000;

DROP TEMPORARY TABLE IF EXISTS seq_500, seq_2000;
SET FOREIGN_KEY_CHECKS = 1;
ANALYZE TABLE posts, comments;

SET @hot_post_id = (SELECT post_id FROM posts WHERE title = '박종준 벤치마크 핫포스트' LIMIT 1);
SELECT @hot_post_id AS hot_post_id,
       (SELECT COUNT(*) FROM comments WHERE post_id = @hot_post_id AND parent_id IS NULL) AS top_level,
       (SELECT COUNT(*) FROM comments WHERE post_id = @hot_post_id AND parent_id IS NOT NULL) AS replies;

-- [F-3] BEFORE — 댓글 1건당 개별 조회 (대댓글 1건 + 작성자 1건) 플랜
EXPLAIN ANALYZE
SELECT * FROM comments WHERE parent_id = (
    SELECT MIN(comment_id) FROM comments WHERE post_id = @hot_post_id AND parent_id IS NULL
);

EXPLAIN ANALYZE
SELECT member_id, name FROM members WHERE member_id = (
    SELECT author_id FROM comments WHERE post_id = @hot_post_id AND parent_id IS NULL LIMIT 1
);

-- [F-4] AFTER — Batch IN 1회로 같은 작업
SET @parent_ids = (SELECT GROUP_CONCAT(comment_id) FROM comments WHERE post_id = @hot_post_id AND parent_id IS NULL);
SET @sql1 = CONCAT('EXPLAIN ANALYZE SELECT * FROM comments WHERE parent_id IN (', @parent_ids, ')');
PREPARE stmt1 FROM @sql1;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;

SET @author_ids = (SELECT GROUP_CONCAT(DISTINCT author_id) FROM comments WHERE post_id = @hot_post_id);
SET @sql2 = CONCAT('EXPLAIN ANALYZE SELECT member_id, name FROM members WHERE member_id IN (', @author_ids, ')');
PREPARE stmt2 FROM @sql2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;
