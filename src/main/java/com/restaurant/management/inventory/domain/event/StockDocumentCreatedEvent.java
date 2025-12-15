package com.restaurant.management.inventory.domain.event;

import com.restaurant.management.common.domain.DomainEvent;
import com.restaurant.management.inventory.domain.model.DocumentType;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 库存单据创建事件
 * 当库存单据被创建时发布此事件
 */
@Getter
public class StockDocumentCreatedEvent implements DomainEvent {
    
    private final String documentId;
    private final DocumentType documentType;
    private final LocalDateTime occurredOn;
    
    public StockDocumentCreatedEvent(String documentId, DocumentType documentType) {
        this.documentId = documentId;
        this.documentType = documentType;
        this.occurredOn = LocalDateTime.now();
    }
    
    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
    
    @Override
    public String eventType() {
        return "StockDocumentCreated";
    }
}