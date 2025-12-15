package com.restaurant.management.inventory.domain.model;

/**
 * 库存单据类型枚举
 */
public enum DocumentType {
    
    // 入库单据类型
    /**
     * 采购入库
     */
    INBOUND_PURCHASE("采购入库"),
    
    /**
     * 生产入库
     */
    INBOUND_PRODUCTION("生产入库"),
    
    /**
     * 退货入库
     */
    INBOUND_RETURN("退货入库"),
    
    /**
     * 其他入库
     */
    INBOUND_OTHER("其他入库"),
    
    // 出库单据类型
    /**
     * 销售出库
     */
    OUTBOUND_SALE("销售出库"),
    
    /**
     * 生产出库
     */
    OUTBOUND_PRODUCTION("生产出库"),
    
    /**
     * 调拨出库
     */
    OUTBOUND_TRANSFER("调拨出库"),
    
    /**
     * 其他出库
     */
    OUTBOUND_OTHER("其他出库"),
    
    // 调拨单据类型
    /**
     * 仓库调拨
     */
    TRANSFER("仓库调拨"),
    
    // 调整单据类型
    /**
     * 库存调整
     */
    ADJUSTMENT("库存调整");
    
    private final String description;
    
    DocumentType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 判断是否为入库单据
     */
    public boolean isInbound() {
        return this == INBOUND_PURCHASE || 
               this == INBOUND_PRODUCTION || 
               this == INBOUND_RETURN || 
               this == INBOUND_OTHER;
    }
    
    /**
     * 判断是否为出库单据
     */
    public boolean isOutbound() {
        return this == OUTBOUND_SALE || 
               this == OUTBOUND_PRODUCTION || 
               this == OUTBOUND_TRANSFER || 
               this == OUTBOUND_OTHER;
    }
    
    /**
     * 判断是否为调拨单据
     */
    public boolean isTransfer() {
        return this == TRANSFER;
    }
    
    /**
     * 判断是否为调整单据
     */
    public boolean isAdjustment() {
        return this == ADJUSTMENT;
    }
}