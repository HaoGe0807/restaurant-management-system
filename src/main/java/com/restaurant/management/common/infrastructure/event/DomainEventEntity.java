package com.restaurant.management.common.infrastructure.event;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.restaurant.management.common.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 领域事件实体（事务性发件箱模式）
 * 用于持久化领域事件，确保事件不丢失
 */
@Getter
@Setter
@TableName("domain_events")
public class DomainEventEntity extends BaseEntity {

    /**
     * 事件类型（类的全限定名）
     */
    private String eventType;
    
    /**
     * 事件内容（JSON格式）
     */
    private String eventData;
    
    /**
     * 事件状态：PENDING-待发布，PUBLISHED-已发布，FAILED-发布失败
     */
    private String status;
    
    /**
     * 重试次数
     */
    private Integer retryCount;
    
    /**
     * 关联的聚合根ID（可选，用于追踪）
     */
    private String aggregateId;
    
    /**
     * 关联的聚合根类型（可选）
     */
    private String aggregateType;
}

