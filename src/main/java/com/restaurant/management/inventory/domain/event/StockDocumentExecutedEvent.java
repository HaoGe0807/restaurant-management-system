package com.restaurant.management.inventory.domain.event;

import com.restaurant.management.common.domain.DomainEvent;
import com.restaurant.management.inventory.domain.model.DocumentType;
import com.restaurant.management.inventory.domain.model.StockDocumentItem;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 库存单据执行事件
 * 当库存单据执行完成时发布此事件
 */
@Getter
public class StockDocumentExecutedEvent implements DomainEvent {
    
    private final String documentId;
    private final DocumentType documentType;
    private final List<StockDocumentItem> items;
    private final LocalDateTime occurredOn;
    
    public StockDocumentExecutedEvent(String documentId, DocumentType documentType, List<StockDocumentItem> items) {
        this.documentId = documentId;
        this.documentType = documentType;
        this.items = items;
        this.occurredOn = LocalDateTime.now();
    }
    
    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
    
    @Override
    public String eventType() {
        return "StockDocumentExecuted";
    }
}