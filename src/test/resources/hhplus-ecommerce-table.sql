CREATE TABLE user (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '유저ID',
    balance DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '잔액'
) COMMENT='유저';

CREATE TABLE balance_history (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'PK',
    user_id BIGINT NOT NULL COMMENT '유저ID',
    balance_type ENUM('CHARGE', 'USE') NOT NULL COMMENT '잔액타입(CHARGE, USE)',
    amount DECIMAL(10,2) NOT NULL COMMENT '금액',
    processed_at DATETIME NOT NULL COMMENT '반영일시',
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) COMMENT='잔액이력';
CREATE INDEX idx_balance_history_user_id_processed_at
ON balance_history (user_id, processed_at);

CREATE TABLE product (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '상품ID',
    price DECIMAL(10,2) NOT NULL COMMENT '금액',
    stock INT NOT NULL COMMENT '재고수량'
) COMMENT='상품';

CREATE TABLE `order` (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '주문ID',
    user_id BIGINT NOT NULL COMMENT '유저ID',
    coupon_id BIGINT NULL COMMENT '쿠폰ID',
    ordered_at DATETIME NOT NULL COMMENT '주문일시',
    total_price DECIMAL(10,2) NOT NULL COMMENT '주문금액',
    discount_rate DECIMAL(5,2) NOT NULL COMMENT '할인율',
    paid_price DECIMAL(10,2) NOT NULL COMMENT '결제금액'
) COMMENT='주문';
CREATE INDEX idx_order_ordered_at
ON `order` (ordered_at);
CREATE INDEX idx_order_user_id
ON `order` (user_id);

CREATE TABLE order_item (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'PK',
    order_id BIGINT NOT NULL COMMENT '주문ID',
    product_id BIGINT NOT NULL COMMENT '상품ID',
    quantity INT NOT NULL COMMENT '수량',
    product_price DECIMAL(10,2) NOT NULL COMMENT '상품금액',
    UNIQUE KEY uq_order_product (order_id, product_id)
) COMMENT='주문상품';
CREATE INDEX idx_order_item_order_id_product_id
ON order_item (order_id, product_id);

CREATE TABLE coupon_policy (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '쿠폰정책ID',
    discount_rate DECIMAL(5,2) NOT NULL COMMENT '할인율',
    total_count INT NOT NULL COMMENT '수량',
    remaining_count INT NOT NULL COMMENT '잔여수량'
) COMMENT='쿠폰정책';

CREATE TABLE user_coupon (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'PK',
    coupon_policy_id BIGINT NOT NULL COMMENT '쿠폰정책ID',
    user_id BIGINT NOT NULL COMMENT '유저ID',
    issued_at DATETIME NOT NULL COMMENT '발급일시',
    status ENUM('ISSUED', 'USED') NOT NULL COMMENT '쿠폰상태',
    version BIGINT NOT NULL COMMENT '버전',
    UNIQUE KEY uq_coupon_policy_user (coupon_policy_id, user_id)
) COMMENT='유저 쿠폰 소유 및 사용 이력';