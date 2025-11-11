package com.restaurant.management.common.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * 聚合根标记接口
 * 聚合根是聚合的入口，负责维护聚合的一致性边界
 * 支持领域事件的发布
 */
public interface AggregateRoot {
    
    /**
     * 获取领域事件列表
     */
    default List<DomainEvent> getDomainEvents() {
        return new ArrayList<>();
    }
    
    /**
     * 添加领域事件
     */
    default void addDomainEvent(DomainEvent event) {
        // 默认实现，子类可以重写
    }
    
    /**
     * 清除领域事件
     */
    default void clearDomainEvents() {
        // 默认实现，子类可以重写
    }
}

