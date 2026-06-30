-- ================================================================
-- 댓글 조회 N+1 최적화 마이그레이션
-- 적용 대상: EC2 Hard-Click DB
-- 관련 PR: fix/comment-n-plus-one
-- ================================================================

-- 1. comments.parent_id 인덱스 추가
--    findByParentIdIn() 풀스캔 → ref 변환
CREATE INDEX IF NOT EXISTS idx_comments_parent_id ON comments (parent_id);

ANALYZE TABLE comments;
