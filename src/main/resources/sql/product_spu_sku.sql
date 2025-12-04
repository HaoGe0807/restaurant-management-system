-- 商品SPU表
CREATE TABLE IF NOT EXISTS `product_spu` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `spu_id` VARCHAR(32) NOT NULL UNIQUE COMMENT '业务SPU ID',
    `spu_name` VARCHAR(255) NOT NULL COMMENT '商品名称',
    `description` TEXT COMMENT '商品描述',
    `status` VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_spu_name` (`spu_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SPU';

-- 商品SKU表
CREATE TABLE IF NOT EXISTS `product_sku` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `sku_id` VARCHAR(32) NOT NULL UNIQUE COMMENT '业务SKU ID',
    `spu_id` VARCHAR(32) NOT NULL COMMENT '关联SPU ID',
    `sku_name` VARCHAR(255) NOT NULL COMMENT 'SKU名称',
    `price` DECIMAL(10,2) NOT NULL COMMENT '售价',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_spu_id` (`spu_id`),
    CONSTRAINT `fk_sku_spu` FOREIGN KEY (`spu_id`) REFERENCES `product_spu` (`spu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SKU';

-- 库存表增加SKU ID字段（若已存在请手动调整）
-- ALTER TABLE `inventories` ADD COLUMN `sku_id` VARCHAR(32) NOT NULL UNIQUE AFTER `id`;

-- 订单项表新增SPU/SKU字段（若已存在请手动调整）
-- ALTER TABLE `order_items`
--     ADD COLUMN `spu_id` VARCHAR(32) AFTER `order_id`,
--     ADD COLUMN `sku_id` VARCHAR(32) AFTER `spu_id`,
--     ADD COLUMN `sku_name` VARCHAR(255) AFTER `sku_id`,
--     DROP COLUMN `product_id`,
--     DROP COLUMN `product_name`;

