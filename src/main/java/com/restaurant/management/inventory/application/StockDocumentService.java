package com.restaurant.management.inventory.application;

import com.restaurant.management.inventory.domain.model.DocumentStatus;
import com.restaurant.management.inventory.domain.model.DocumentType;
import com.restaurant.management.inventory.domain.model.StockDocument;
import com.restaurant.management.inventory.domain.repository.StockDocumentRepository;
import com.restaurant.management.inventory.domain.service.StockDocumentExecutionEngine;
import com.restaurant.management.inventory.domain.service.StockDocumentFactory;
import com.restaurant.management.inventory.domain.service.StockDocumentStateMachine;
import com.restaurant.management.common.domain.DomainEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 库存单据应用服务
 * 协调库存单据的完整业务流程，包括创建、审核、执行等
 */
@Service
@Transactional
public class StockDocumentService {
    
    @Autowired
    private StockDocumentRepository stockDocumentRepository;
    
    @Autowired
    private StockDocumentFactory stockDocumentFactory;
    
    @Autowired
    private StockDocumentStateMachine stateMachine;
    
    @Autowired
    private StockDocumentExecutionEngine executionEngine;
    
    @Autowired
    private DomainEventPublisher domainEventPublisher;
    
    /**
     * 创建采购入库单
     */
    public StockDocument createPurchaseInboundDocument(String warehouseId, String operatorId,
                                                      List<StockDocumentFactory.PurchaseItem> purchaseItems, 
                                                      String remark) {
        StockDocument document = stockDocumentFactory.createPurchaseInboundDocument(
            warehouseId, operatorId, purchaseItems, remark);
        
        StockDocument savedDocument = stockDocumentRepository.save(document);
        
        // 发布领域事件
        domainEventPublisher.publishAll(savedDocument.getDomainEvents());
        savedDocument.clearDomainEvents();
        
        return savedDocument;
    }
    
    /**
     * 创建销售出库单
     */
    public StockDocument createSaleOutboundDocument(String warehouseId, String operatorId,
                                                   List<StockDocumentFactory.SaleItem> saleItems, 
                                                   String remark) {
        StockDocument document = stockDocumentFactory.createSaleOutboundDocument(
            warehouseId, operatorId, saleItems, remark);
        
        StockDocument savedDocument = stockDocumentRepository.save(document);
        
        // 发布领域事件
        domainEventPublisher.publishAll(savedDocument.getDomainEvents());
        savedDocument.clearDomainEvents();
        
        return savedDocument;
    }
    
    /**
     * 创建调拨单
     */
    public StockDocument createTransferDocument(String sourceWarehouseId, String targetWarehouseId,
                                               String operatorId, List<StockDocumentFactory.TransferItem> transferItems, 
                                               String remark) {
        StockDocument document = stockDocumentFactory.createTransferDocument(
            sourceWarehouseId, targetWarehouseId, operatorId, transferItems, remark);
        
        StockDocument savedDocument = stockDocumentRepository.save(document);
        
        // 发布领域事件
        domainEventPublisher.publishAll(savedDocument.getDomainEvents());
        savedDocument.clearDomainEvents();
        
        return savedDocument;
    }
    
    /**
     * 创建库存调整单
     */
    public StockDocument createAdjustmentDocument(String warehouseId, String operatorId,
                                                 List<StockDocumentFactory.AdjustmentItem> adjustmentItems, 
                                                 String remark) {
        StockDocument document = stockDocumentFactory.createAdjustmentDocument(
            warehouseId, operatorId, adjustmentItems, remark);
        
        StockDocument savedDocument = stockDocumentRepository.save(document);
        
        // 发布领域事件
        domainEventPublisher.publishAll(savedDocument.getDomainEvents());
        savedDocument.clearDomainEvents();
        
        return savedDocument;
    }
    
    /**
     * 提交单据审核
     */
    public void submitDocumentForApproval(String documentId) {
        StockDocument document = getDocumentById(documentId);
        
        // 验证是否可以提交审核
        stateMachine.validateSubmitForApproval(document);
        
        // 提交审核
        document.submitForApproval();
        
        // 保存变更
        stockDocumentRepository.save(document);
        
        // 发布领域事件
        domainEventPublisher.publishAll(document.getDomainEvents());
        document.clearDomainEvents();
    }
    
    /**
     * 审核通过单据
     */
    public void approveDocument(String documentId, String approverId) {
        StockDocument document = getDocumentById(documentId);
        
        // 验证是否可以审核
        stateMachine.validateApproval(document, approverId);
        
        // 审核通过
        document.approve(approverId);
        
        // 保存变更
        stockDocumentRepository.save(document);
        
        // 发布领域事件
        domainEventPublisher.publishAll(document.getDomainEvents());
        document.clearDomainEvents();
    }
    
    /**
     * 拒绝单据
     */
    public void rejectDocument(String documentId, String approverId, String rejectionReason) {
        StockDocument document = getDocumentById(documentId);
        
        // 验证是否可以拒绝
        stateMachine.validateRejection(document, approverId, rejectionReason);
        
        // 拒绝单据
        document.reject(approverId, rejectionReason);
        
        // 保存变更
        stockDocumentRepository.save(document);
        
        // 发布领域事件
        domainEventPublisher.publishAll(document.getDomainEvents());
        document.clearDomainEvents();
    }
    
    /**
     * 执行单据
     */
    public void executeDocument(String documentId) {
        StockDocument document = getDocumentById(documentId);
        
        // 验证是否可以执行
        stateMachine.validateExecution(document);
        
        // 验证执行前的库存状态
        executionEngine.validateInventoryBeforeExecution(document);
        
        // 执行单据
        document.execute();
        executionEngine.executeDocument(document);
        
        // 保存变更
        stockDocumentRepository.save(document);
        
        // 发布领域事件
        domainEventPublisher.publishAll(document.getDomainEvents());
        document.clearDomainEvents();
    }
    
    /**
     * 取消单据
     */
    public void cancelDocument(String documentId, String cancelReason) {
        StockDocument document = getDocumentById(documentId);
        
        // 验证是否可以取消
        stateMachine.validateCancellation(document, cancelReason);
        
        // 取消单据
        document.cancel(cancelReason);
        
        // 保存变更
        stockDocumentRepository.save(document);
        
        // 发布领域事件
        domainEventPublisher.publishAll(document.getDomainEvents());
        document.clearDomainEvents();
    }
    
    /**
     * 根据ID获取单据
     */
    @Transactional(readOnly = true)
    public StockDocument getDocumentById(String documentId) {
        return stockDocumentRepository.findByDocumentId(documentId)
            .orElseThrow(() -> new IllegalArgumentException("单据不存在: " + documentId));
    }
    
    /**
     * 根据单据编号获取单据
     */
    @Transactional(readOnly = true)
    public StockDocument getDocumentByNo(String documentNo) {
        return stockDocumentRepository.findByDocumentNo(documentNo)
            .orElseThrow(() -> new IllegalArgumentException("单据不存在: " + documentNo));
    }
    
    /**
     * 获取待审核单据列表
     */
    @Transactional(readOnly = true)
    public List<StockDocument> getPendingDocuments() {
        return stockDocumentRepository.findPendingDocuments();
    }
    
    /**
     * 获取已审核但未执行的单据列表
     */
    @Transactional(readOnly = true)
    public List<StockDocument> getApprovedButNotExecutedDocuments() {
        return stockDocumentRepository.findApprovedButNotExecutedDocuments();
    }
    
    /**
     * 根据条件查询单据列表
     */
    @Transactional(readOnly = true)
    public List<StockDocument> findDocumentsByConditions(DocumentType type, DocumentStatus status,
                                                         String warehouseId, LocalDateTime startTime,
                                                         LocalDateTime endTime, int offset, int limit) {
        return stockDocumentRepository.findByConditionsWithPagination(
            type, status, warehouseId, startTime, endTime, offset, limit);
    }
    
    /**
     * 获取单据的可用操作列表
     */
    @Transactional(readOnly = true)
    public List<DocumentStatus> getAvailableActions(String documentId) {
        StockDocument document = getDocumentById(documentId);
        return stateMachine.getAvailableTransitions(document.getStatus());
    }
    
    /**
     * 检查单据是否可以编辑
     */
    @Transactional(readOnly = true)
    public boolean canEditDocument(String documentId) {
        StockDocument document = getDocumentById(documentId);
        return stateMachine.canEdit(document.getStatus());
    }
    
    /**
     * 批量审核单据
     */
    public void batchApproveDocuments(List<String> documentIds, String approverId) {
        for (String documentId : documentIds) {
            try {
                approveDocument(documentId, approverId);
            } catch (Exception e) {
                // 记录错误但继续处理其他单据
                // 这里可以添加日志记录
                System.err.println("批量审核失败 - 单据ID: " + documentId + ", 错误: " + e.getMessage());
            }
        }
    }
    
    /**
     * 批量执行单据
     */
    public void batchExecuteDocuments(List<String> documentIds) {
        for (String documentId : documentIds) {
            try {
                executeDocument(documentId);
            } catch (Exception e) {
                // 记录错误但继续处理其他单据
                System.err.println("批量执行失败 - 单据ID: " + documentId + ", 错误: " + e.getMessage());
            }
        }
    }
    
    /**
     * 获取单据统计信息
     */
    @Transactional(readOnly = true)
    public DocumentStatistics getDocumentStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        DocumentStatistics statistics = new DocumentStatistics();
        
        statistics.setTotalCount(stockDocumentRepository.countByCreateTimeBetween(startTime, endTime));
        statistics.setPendingCount(stockDocumentRepository.countByStatus(DocumentStatus.PENDING));
        statistics.setApprovedCount(stockDocumentRepository.countByStatus(DocumentStatus.APPROVED));
        statistics.setExecutedCount(stockDocumentRepository.countByStatus(DocumentStatus.EXECUTED));
        statistics.setCancelledCount(stockDocumentRepository.countByStatus(DocumentStatus.CANCELLED));
        
        return statistics;
    }
    
    /**
     * 单据统计信息类
     */
    public static class DocumentStatistics {
        private long totalCount;
        private long pendingCount;
        private long approvedCount;
        private long executedCount;
        private long cancelledCount;
        
        // Getters and Setters
        public long getTotalCount() { return totalCount; }
        public void setTotalCount(long totalCount) { this.totalCount = totalCount; }
        
        public long getPendingCount() { return pendingCount; }
        public void setPendingCount(long pendingCount) { this.pendingCount = pendingCount; }
        
        public long getApprovedCount() { return approvedCount; }
        public void setApprovedCount(long approvedCount) { this.approvedCount = approvedCount; }
        
        public long getExecutedCount() { return executedCount; }
        public void setExecutedCount(long executedCount) { this.executedCount = executedCount; }
        
        public long getCancelledCount() { return cancelledCount; }
        public void setCancelledCount(long cancelledCount) { this.cancelledCount = cancelledCount; }
    }
}