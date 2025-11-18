-- 领域事件表（事务性发件箱模式）
CREATE TABLE IF NOT EXISTS `domain_events` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `event_type` VARCHAR(255) NOT NULL COMMENT '事件类型（类的全限定名）',
    `event_data` TEXT NOT NULL COMMENT '事件内容（JSON格式）',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '事件状态：PENDING-待发布，PUBLISHED-已发布，FAILED-发布失败',
    `retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    `aggregate_id` VARCHAR(100) COMMENT '关联的聚合根ID',
    `aggregate_type` VARCHAR(100) COMMENT '关联的聚合根类型',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_status` (`status`),
    INDEX `idx_aggregate` (`aggregate_type`, `aggregate_id`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='领域事件表（事务性发件箱）';

