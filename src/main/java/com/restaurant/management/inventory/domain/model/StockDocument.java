package com.restaurant.management.inventory.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.restaurant.management.common.domain.AggregateRoot;
import com.restaurant.management.common.domain.BaseEntity;
import com.restaurant.management.common.domain.DomainEvent;
import com.restaurant.management.inventory.domain.event.StockDocumentCreatedEvent;
import com.restaurant.management.inventory.domain.event.StockDocumentApprovedEvent;
import com.restaurant.management.inventory.domain.event.StockDocumentExecutedEvent;
import com.restaurant.management.inventory.domain.event.StockDocumentCancelledEvent;
import com.restaurant.management.inventory.domain.event.StockDocumentRejectedEvent;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 库存单据聚合根
 * 管理库存单据的完整生命周期，包括创建、审核、执行等
 */
@Getter
@Setter
@TableName("stock_document")
public class StockDocument extends BaseEntity implements AggregateRoot {
    
    /**
     * 存储该聚合根产生的所有领域事件（仅存在于内存）
     */
    @TableField(exist = false)
    private List<DomainEvent> domainEvents = new ArrayList<>();
    
    /**
     * 单据明细（不持久化到数据库，通过Repository加载）
     */
    @TableField(exist = false)
    private List<StockDocumentItem> items = new ArrayList<>();
    
    /**
     * 单据ID（业务主键）
     */
    @TableField("document_id")
    private String documentId;
    
    /**
     * 单据编号
     */
    @TableField("document_no")
    private String documentNo;
    
    /**
     * 单据类型
     */
    @TableField("document_type")
    private DocumentType type;
    
    /**
     * 单据状态
     */
    @TableField("document_status")
    private DocumentStatus status;
    
    /**
     * 仓库ID
     */
    @TableField("warehouse_id")
    private String warehouseId;
    
    /**
     * 操作人ID
     */
    @TableField("operator_id")
    private String operatorId;
    
    /**
     * 总金额
     */
    @TableField("total_amount")
    private BigDecimal totalAmount;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 审核时间
     */
    @TableField("approve_time")
    private LocalDateTime approveTime;
    
    /**
     * 执行时间
     */
    @TableField("execute_time")
    private LocalDateTime executeTime;
    
    /**
     * 创建库存单据
     */
    public static StockDocument create(DocumentType type, String warehouseId, String operatorId, 
                                     List<StockDocumentItem> items, String remark) {
        StockDocument document = new StockDocument();
        document.documentId = generateDocumentId();
        document.documentNo = generateDocumentNo(type, warehouseId);
        document.type = type;
        document.status = DocumentStatus.DRAFT;
        document.warehouseId = warehouseId;
        document.operatorId = operatorId;
        document.remark = remark;
        document.items = items != null ? items : new ArrayList<>();
        
        // 设置单据明细的文档ID
        document.items.forEach(item -> item.setDocumentId(document.documentId));
        
        // 计算总金额
        document.calculateTotalAmount();
        
        // 发布单据创建事件
        document.addDomainEvent(new StockDocumentCreatedEvent(document.documentId, document.type));
        
        return document;
    }
    
    /**
     * 提交审核
     */
    public void submitForApproval() {
        if (status != DocumentStatus.DRAFT) {
            throw new IllegalStateException("只有草稿状态的单据才能提交审核");
        }
        
        // 验证单据完整性
        validateDocumentCompleteness();
        
        // 更新状态
        this.status = DocumentStatus.PENDING;
        
        // 发布提交审核事件
        addDomainEvent(new StockDocumentCreatedEvent(this.documentId, this.type));
    }
    
    /**
     * 审核通过
     */
    public void approve(String approverId) {
        if (status != DocumentStatus.PENDING) {
            throw new IllegalStateException("只有待审核状态的单据才能审核");
        }
        
        // 更新状态
        this.status = DocumentStatus.APPROVED;
        this.approveTime = LocalDateTime.now();
        
        // 发布审核通过事件
        addDomainEvent(new StockDocumentApprovedEvent(this.documentId, this.type, approverId));
    }
    
    /**
     * 审核拒绝
     */
    public void reject(String approverId, String rejectionReason) {
        if (status != DocumentStatus.PENDING) {
            throw new IllegalStateException("只有待审核状态的单据才能拒绝");
        }
        
        // 更新状态
        this.status = DocumentStatus.REJECTED;
        this.remark = this.remark + " [拒绝原因: " + rejectionReason + "]";
        
        // 发布审核拒绝事件
        addDomainEvent(new StockDocumentRejectedEvent(this.documentId, this.type, approverId, rejectionReason));
    }
    
    /**
     * 执行单据
     */
    public void execute() {
        if (status != DocumentStatus.APPROVED) {
            throw new IllegalStateException("只有已审核状态的单据才能执行");
        }
        
        // 更新状态
        this.status = DocumentStatus.EXECUTED;
        this.executeTime = LocalDateTime.now();
        
        // 发布单据执行事件
        addDomainEvent(new StockDocumentExecutedEvent(this.documentId, this.type, this.items));
    }
    
    /**
     * 取消单据
     */
    public void cancel(String cancelReason) {
        if (status == DocumentStatus.EXECUTED) {
            throw new IllegalStateException("已执行的单据不能取消");
        }
        
        this.status = DocumentStatus.CANCELLED;
        this.remark = cancelReason;
        
        // 发布单据取消事件
        addDomainEvent(new StockDocumentCancelledEvent(this.documentId, this.type, cancelReason));
    }
    
    /**
     * 添加单据明细
     */
    public void addItem(StockDocumentItem item) {
        if (item == null) {
            return;
        }
        
        if (status != DocumentStatus.DRAFT) {
            throw new IllegalStateException("只有草稿状态的单据才能修改明细");
        }
        
        item.setDocumentId(this.documentId);
        this.items.add(item);
        calculateTotalAmount();
    }
    
    /**
     * 移除单据明细
     */
    public void removeItem(String skuId) {
        if (status != DocumentStatus.DRAFT) {
            throw new IllegalStateException("只有草稿状态的单据才能修改明细");
        }
        
        this.items.removeIf(item -> item.getSkuId().equals(skuId));
        calculateTotalAmount();
    }
    
    /**
     * 计算总金额
     */
    private void calculateTotalAmount() {
        this.totalAmount = items.stream()
            .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * 验证单据完整性
     */
    private void validateDocumentCompleteness() {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("单据明细不能为空");
        }
        
        for (StockDocumentItem item : items) {
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("商品数量必须大于0");
            }
            if (item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("商品单价不能为负数");
            }
        }
    }
    
    /**
     * 生成单据ID
     */
    private static String generateDocumentId() {
        return "DOC" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
    
    /**
     * 生成单据编号
     */
    private static String generateDocumentNo(DocumentType type, String warehouseId) {
        String typeCode = getTypeCode(type);
        String dateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequence = String.format("%04d", System.currentTimeMillis() % 10000);
        return String.format("%s-%s-%s-%s", typeCode, warehouseId, dateStr, sequence);
    }
    
    /**
     * 获取单据类型代码
     */
    private static String getTypeCode(DocumentType type) {
        switch (type) {
            case INBOUND_PURCHASE: return "PI";
            case INBOUND_PRODUCTION: return "MI";
            case INBOUND_RETURN: return "RI";
            case OUTBOUND_SALE: return "SO";
            case OUTBOUND_PRODUCTION: return "MO";
            case OUTBOUND_TRANSFER: return "TO";
            case TRANSFER: return "TR";
            case ADJUSTMENT: return "AD";
            default: return "OT";
        }
    }
    
    @Override
    public List<DomainEvent> getDomainEvents() {
        return domainEvents;
    }
    
    @Override
    public void addDomainEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }
    
    @Override
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}