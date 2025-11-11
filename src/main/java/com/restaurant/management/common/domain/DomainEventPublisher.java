package com.restaurant.management.common.domain;

import java.util.List;

/**
 * 领域事件发布器接口
 * 由基础设施层实现
 */
public interface DomainEventPublisher {
    
    /**
     * 发布单个领域事件
     */
    void publish(DomainEvent event);
    
    /**
     * 发布多个领域事件
     */
    void publishAll(List<DomainEvent> events);
}

