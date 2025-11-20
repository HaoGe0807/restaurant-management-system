package com.restaurant.management.common.infrastructure.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.management.common.domain.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 领域事件发件箱出库任务
 * 定期扫描 PENDING 事件，尝试发布并更新状态
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventOutboxProcessor {

    private final DomainEventMapper domainEventMapper;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Value("${domain-event.outbox.batch-size:50}")
    private int batchSize;

    @Value("${domain-event.outbox.max-retry:5}")
    private int maxRetry;

    /**
     * 固定间隔扫描发件箱
     */
    @Scheduled(fixedDelayString = "${domain-event.outbox.fixed-delay-ms:2000}")
    public void publishPendingEvents() {
        List<DomainEventEntity> pendingEvents = domainEventMapper.findPendingEvents(batchSize);
        for (DomainEventEntity entity : pendingEvents) {
            boolean locked = domainEventMapper.markProcessing(entity.getId()) == 1;
            if (!locked) {
                continue;
            }
            int currentRetry = entity.getRetryCount() + 1;
            try {
                DomainEvent event = deserializeEvent(entity);
                applicationEventPublisher.publishEvent(java.util.Objects.requireNonNull(event));
                domainEventMapper.markPublished(entity.getId());
                log.debug("领域事件发布成功，事件ID: {}", entity.getId());
            } catch (Exception ex) {
                log.error("领域事件发布失败，事件ID: {}", entity.getId(), ex);
                handleFailure(entity.getId(), currentRetry, ex.getMessage());
            }
        }
    }

    private DomainEvent deserializeEvent(DomainEventEntity entity) throws Exception {
        Class<?> eventClass = Class.forName(entity.getEventType());
        return (DomainEvent) objectMapper.readValue(entity.getEventData(), eventClass);
    }

    private void handleFailure(Long eventId, int retryCount, String errorMessage) {
        if (retryCount >= maxRetry) {
            domainEventMapper.markFailed(eventId, truncateError(errorMessage));
        } else {
            domainEventMapper.markPending(eventId, truncateError(errorMessage));
        }
    }

    private String truncateError(String message) {
        if (message == null) {
            return null;
        }
        return message.length() > 500 ? message.substring(0, 500) : message;
    }
}


