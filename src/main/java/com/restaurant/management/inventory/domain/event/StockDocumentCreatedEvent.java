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
public class StockDocumentCreatedEvent extends DomainEvent {
    
    private final String documentId;
    private final DocumentType documentType;
    
    public StockDocumentCreatedEvent(String documentId, DocumentType documentType) {
        super();
        this.documentId = documentId;
        this.documentType = documentType;
    }
}