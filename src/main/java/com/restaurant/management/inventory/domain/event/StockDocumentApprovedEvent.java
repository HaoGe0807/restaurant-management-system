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
public class StockDocumentApprovedEvent extends DomainEvent {
    
    private final String documentId;
    private final DocumentType documentType;
    private final String approverId;
    
    public StockDocumentApprovedEvent(String documentId, DocumentType documentType, String approverId) {
        super();
        this.documentId = documentId;
        this.documentType = documentType;
        this.approverId = approverId;
    }
}