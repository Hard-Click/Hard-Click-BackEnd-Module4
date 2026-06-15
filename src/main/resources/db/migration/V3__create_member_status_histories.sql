CREATE TABLE member_status_histories (
    history_id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    previous_status VARCHAR(20) NOT NULL,
    changed_status VARCHAR(20) NOT NULL,
    memo VARCHAR(500) DEFAULT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (history_id),
    INDEX idx_member_status_histories_member_id (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
