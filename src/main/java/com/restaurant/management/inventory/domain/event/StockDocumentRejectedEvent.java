package com.restaurant.management.inventory.domain.event;

import com.restaurant.management.common.domain.DomainEvent;
import com.restaurant.management.inventory.domain.model.DocumentType;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 库存单据审核拒绝事件
 * 当库存单据审核被拒绝时发布此事件
 */
@Getter
public class StockDocumentRejectedEvent extends DomainEvent {
    
    private final String documentId;
    private final DocumentType documentType;
    private final String approverId;
    private final String rejectionReason;
    
    public StockDocumentRejectedEvent(String documentId, DocumentType documentType, String approverId, String rejectionReason) {
        super();
        this.documentId = documentId;
        this.documentType = documentType;
        this.approverId = approverId;
        this.rejectionReason = rejectionReason;
    }
}