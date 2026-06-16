CREATE TABLE `cart_items` (
  `cart_item_id` BIGINT       NOT NULL AUTO_INCREMENT,
  `member_id`    BIGINT       NOT NULL,
  `course_id`    BIGINT       NOT NULL,
  `added_at`     DATETIME(6)  NOT NULL,
  PRIMARY KEY (`cart_item_id`),
  UNIQUE KEY `uk_cart_member_course` (`member_id`, `course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
