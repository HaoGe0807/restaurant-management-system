package com.restaurant.management.common.infrastructure.event;

/**
 * 领域事件发件箱状态
 */
public enum DomainEventStatus {
    PENDING,
    PROCESSING,
    PUBLISHED,
    FAILED
}


