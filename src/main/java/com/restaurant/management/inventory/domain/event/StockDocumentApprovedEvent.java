package com.restaurant.management.inventory.domain.event;

import com.restaurant.management.common.domain.DomainEvent;
import com.restaurant.management.inventory.domain.model.DocumentType;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 库存单据审核通过事件
 * 当库存单据审核通过时发布此事件
 */
@Getter
public class StockDocumentApprovedEvent implements DomainEvent {
    
    private final String documentId;
    private final DocumentType documentType;
    private final String approverId;
    private final LocalDateTime occurredOn;
    
    public StockDocumentApprovedEvent(String documentId, DocumentType documentType, String approverId) {
        this.documentId = documentId;
        this.documentType = documentType;
        this.approverId = approverId;
        this.occurredOn = LocalDateTime.now();
    }
    
    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
    
    @Override
    public String eventType() {
        return "StockDocumentApproved";
    }
}