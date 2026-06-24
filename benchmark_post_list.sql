-- ================================================================
--  게시글 목록 조회 병목 벤치마크 (담당: 윤종호 — 방법 ① 인덱스 / ④ 비정규화)
--  실제 스키마(박종준 공유 DDL) 기준, 단일서버(2vCPU/2GB) 규모 축소판
--    members  1,000
--    posts   10,000
--    comments 100,000  (일부 게시글에 편중 분포 — 정렬 병목 재현용)
-- ================================================================

-- ================================================================
-- [A] 대량 더미 데이터 시딩
-- ================================================================
SET FOREIGN_KEY_CHECKS = 0;
SET cte_max_recursion_depth = 200000;

-- A-1. members 1,000건
INSERT INTO members
    (username, email, password, name, gender, birth_date, phone_number,
     role, status, is_password_change_required, login_fail_count,
     is_locked, optional_terms_agreed, created_at, updated_at)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 1000
)
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
FROM seq;

-- A-2. posts 10,000건 — board_type/status 분산, author는 위 members 중에서 순환 배정
INSERT INTO posts
    (author_id, board_type, title, content, view_count, status, is_accepted, created_at, updated_at)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 10000
)
SELECT
    ((n - 1) % 1000) + (SELECT MIN(member_id) FROM members WHERE username LIKE 'bench_user_%'),
    IF(n % 2 = 0, 'FREE', 'QUESTION'),
    CONCAT('벤치마크 게시글 ', n),
    CONCAT('벤치마크용 본문 내용입니다. 게시글 번호 ', n),
    FLOOR(RAND() * 500),
    'ACTIVE',
    0,
    NOW() - INTERVAL (10000 - n) MINUTE,
    NOW() - INTERVAL (10000 - n) MINUTE
FROM seq;

-- A-3. comments 100,000건 — 일부 게시글(상위 10%)에 댓글이 편중되게 분포
--      편중 없이 균등 분포만 만들면 "정렬 병목"이 과소평가되므로 인기글 패턴을 재현한다.
INSERT INTO comments
    (post_id, author_id, parent_id, content, is_accepted, is_deleted, status, created_at, updated_at)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 100000
)
SELECT
    CASE
        -- 70%의 댓글은 인기글 1,000건(전체 게시글의 상위 10%)에 집중
        WHEN n % 10 < 7 THEN (SELECT MIN(post_id) FROM posts) + (n % 1000)
        -- 나머지 30%는 전체 게시글에 균등 분포
        ELSE (SELECT MIN(post_id) FROM posts) + (n % 10000)
    END,
    (SELECT MIN(member_id) FROM members WHERE username LIKE 'bench_user_%') + (n % 1000),
    NULL,
    CONCAT('벤치마크 댓글 ', n),
    0, 0, 'ACTIVE',
    NOW() - INTERVAL (100000 - n) SECOND,
    NOW() - INTERVAL (100000 - n) SECOND
FROM seq;

SET FOREIGN_KEY_CHECKS = 1;
ANALYZE TABLE members, posts, comments;


-- ================================================================
-- [B] BEFORE — 인덱스 없는 상태에서 현재 쿼리 구조 측정
--     (목록 필터 + 댓글수 정렬 상관 서브쿼리)
-- ================================================================
EXPLAIN ANALYZE
SELECT p.post_id, p.title, p.author_id, p.created_at,
       (SELECT COUNT(*) FROM comments c WHERE c.post_id = p.post_id) AS comment_count
FROM posts p
WHERE p.board_type = 'FREE' AND p.status = 'ACTIVE'
ORDER BY (SELECT COUNT(*) FROM comments c WHERE c.post_id = p.post_id) DESC
LIMIT 20 OFFSET 0;

-- 댓글수 카운트 단건 패턴 (N+1 재현용)
EXPLAIN ANALYZE
SELECT COUNT(*) FROM comments WHERE post_id = (SELECT MIN(post_id) FROM posts);


-- ================================================================
-- [C] 방법 ① 인덱스 추가
-- ================================================================
CREATE INDEX idx_comments_post_id   ON comments (post_id);
CREATE INDEX idx_comments_author_id ON comments (author_id);
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
-- [E] 방법 ④ 비정규화 comment_count
-- ================================================================
ALTER TABLE posts ADD COLUMN comment_count INT NOT NULL DEFAULT 0;

-- 백필: posts 10,000건 기준 1회성 풀스캔 (운영에서는 배치 시간대에 1회만 실행)
UPDATE posts p
SET p.comment_count = (
    SELECT COUNT(*) FROM comments c WHERE c.post_id = p.post_id
);

-- 정렬까지 인덱스로 해결하는 커버링 인덱스
CREATE INDEX idx_posts_board_status_count ON posts (board_type, status, comment_count);
ANALYZE TABLE posts;

-- AFTER(비정규화) — COUNT 서브쿼리 자체가 사라진 최종 쿼리
EXPLAIN ANALYZE
SELECT p.post_id, p.title, p.author_id, p.created_at, p.comment_count
FROM posts p
WHERE p.board_type = 'FREE' AND p.status = 'ACTIVE'
ORDER BY p.comment_count DESC
LIMIT 20 OFFSET 0;


-- ================================================================
-- [F] 정합성 검증 — 백필 결과가 실제 댓글 수와 일치하는지 샘플 확인
-- ================================================================
SELECT p.post_id, p.comment_count AS denormalized,
       (SELECT COUNT(*) FROM comments c WHERE c.post_id = p.post_id) AS actual
FROM posts p
ORDER BY p.post_id
LIMIT 20;
