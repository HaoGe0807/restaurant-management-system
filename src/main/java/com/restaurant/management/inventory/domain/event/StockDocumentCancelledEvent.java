package com.restaurant.management.inventory.domain.event;

import com.restaurant.management.common.domain.DomainEvent;
import com.restaurant.management.inventory.domain.model.DocumentType;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 库存单据取消事件
 * 当库存单据被取消时发布此事件
 */
@Getter
public class StockDocumentCancelledEvent implements DomainEvent {
    
    private final String documentId;
    private final DocumentType documentType;
    private final String cancelReason;
    private final LocalDateTime occurredOn;
    
    public StockDocumentCancelledEvent(String documentId, DocumentType documentType, String cancelReason) {
        this.documentId = documentId;
        this.documentType = documentType;
        this.cancelReason = cancelReason;
        this.occurredOn = LocalDateTime.now();
    }
    
    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
    
    @Override
    public String eventType() {
        return "StockDocumentCancelled";
    }
}