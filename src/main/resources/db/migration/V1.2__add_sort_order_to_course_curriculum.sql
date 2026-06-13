ALTER TABLE course_curriculum ADD COLUMN sort_order INT NOT NULL DEFAULT 0;
ALTER TABLE email_verifications ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING';
ALTER TABLE email_verifications ADD COLUMN used BIT(1) NOT NULL DEFAULT 0;
CREATE TABLE video (
                       video_id bigint NOT NULL,
                       curriculum_id bigint NOT NULL,
                       duration_seconds int NOT NULL,
                       is_preview bit(1) NOT NULL,
                       video_url varchar(255) NOT NULL,
                       sort_order int DEFAULT NULL,
                       PRIMARY KEY (video_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;