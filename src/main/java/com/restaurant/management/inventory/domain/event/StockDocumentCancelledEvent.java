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
public class StockDocumentCancelledEvent extends DomainEvent {
    
    private final String documentId;
    private final DocumentType documentType;
    private final String cancelReason;
    
    public StockDocumentCancelledEvent(String documentId, DocumentType documentType, String cancelReason) {
        super();
        this.documentId = documentId;
        this.documentType = documentType;
        this.cancelReason = cancelReason;
    }
}