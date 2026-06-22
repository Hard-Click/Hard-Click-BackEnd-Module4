-- 강의 목록/결제내역 N+1 + 풀스캔 최적화용 인덱스

CREATE INDEX idx_course_status_created_at ON course (status, created_at);
CREATE INDEX idx_course_author_id ON course (author_id);

CREATE INDEX idx_reviews_course_id ON reviews (course_id);
CREATE INDEX idx_enrollments_course_id ON enrollments (course_id);

CREATE INDEX idx_payments_member_id ON payments (member_id);
CREATE INDEX idx_order_items_order_id ON order_items (order_id);
