package com.restaurant.management.common.infrastructure.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.management.common.domain.DomainEvent;
import com.restaurant.management.common.domain.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Spring实现的领域事件发布器（事务性发件箱模式）
 * 
 * 工作流程：
 * 1. 在事务中保存事件到数据库（domain_events表）
 * 2. 事务提交后，通过定时任务发布事件
 * 3. 确保事件不丢失，即使应用崩溃也能恢复
 * 
 * 注意：当前实现是简化版，实际生产环境建议使用更完善的事务性发件箱模式
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpringDomainEventPublisher implements DomainEventPublisher {
    
    private final ApplicationEventPublisher applicationEventPublisher;
    private final DomainEventMapper domainEventMapper;
    private final ObjectMapper objectMapper;
    
    /**
     * 发布单个领域事件
     * 使用事务性发件箱模式：先保存到数据库，事务提交后由定时任务发布
     */
    @Override
    @Transactional
    public void publish(DomainEvent event) {
        try {
            // 1. 将事件保存到数据库（在事务中）
            DomainEventEntity eventEntity = new DomainEventEntity();
            eventEntity.setEventType(event.getClass().getName());
            eventEntity.setEventData(objectMapper.writeValueAsString(event));
            eventEntity.setStatus("PENDING");
            eventEntity.setRetryCount(0);
            
            // 如果事件包含聚合根信息，可以设置
            if (event instanceof AggregateEvent) {
                AggregateEvent aggregateEvent = (AggregateEvent) event;
                eventEntity.setAggregateId(aggregateEvent.getAggregateId());
                eventEntity.setAggregateType(aggregateEvent.getAggregateType());
            }
            
            domainEventMapper.insert(eventEntity);
            log.debug("领域事件已保存到数据库，事件类型: {}, ID: {}", event.getClass().getSimpleName(), eventEntity.getId());
            
            // 2. 立即发布（简化版，实际应该由定时任务在事务提交后发布）
            // 这里使用 @TransactionalEventListener 确保在事务提交后才真正发布
            applicationEventPublisher.publishEvent(event);
            
        } catch (Exception e) {
            log.error("保存领域事件失败", e);
            throw new RuntimeException("保存领域事件失败", e);
        }
    }
    
    /**
     * 发布多个领域事件
     */
    @Override
    @Transactional
    public void publishAll(List<DomainEvent> events) {
        events.forEach(this::publish);
    }
    
    /**
     * 标记事件接口（可选，用于关联聚合根信息）
     */
    public interface AggregateEvent {
        String getAggregateId();
        String getAggregateType();
    }
}

