-- orders: status, plan_id 컬럼 추가 + AUTO_INCREMENT
ALTER TABLE `orders`
    ADD COLUMN `status`  VARCHAR(20) NOT NULL DEFAULT 'PENDING' AFTER `payment_type`,
    ADD COLUMN `plan_id` BIGINT      DEFAULT NULL               AFTER `status`,
    MODIFY COLUMN `order_id` BIGINT NOT NULL AUTO_INCREMENT;

-- order_items: 주문 시 캡처한 강의 정보 + AUTO_INCREMENT
ALTER TABLE `order_items`
    ADD COLUMN `course_title` VARCHAR(255) NOT NULL DEFAULT '' AFTER `course_id`,
    ADD COLUMN `price`        INT          NOT NULL DEFAULT 0   AFTER `course_title`,
    MODIFY COLUMN `order_item_id` BIGINT NOT NULL AUTO_INCREMENT;

-- subscription_plans: 가격 + 기간 추가 + AUTO_INCREMENT
ALTER TABLE `subscription_plans`
    ADD COLUMN `price`           INT NOT NULL DEFAULT 0  AFTER `name`,
    ADD COLUMN `duration_months` INT NOT NULL DEFAULT 12 AFTER `price`,
    MODIFY COLUMN `plan_id` BIGINT NOT NULL AUTO_INCREMENT;

-- payments: Toss 결제키 추가 + AUTO_INCREMENT
ALTER TABLE `payments`
    ADD COLUMN `toss_payment_key` VARCHAR(200) DEFAULT NULL AFTER `status`,
    MODIFY COLUMN `payment_id` BIGINT NOT NULL AUTO_INCREMENT;

-- subscriptions: AUTO_INCREMENT
ALTER TABLE `subscriptions`
    MODIFY COLUMN `subscription_id` BIGINT NOT NULL AUTO_INCREMENT;
