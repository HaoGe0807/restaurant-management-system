package com.restaurant.management.inventory.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.restaurant.management.common.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 库存单据明细实体
 * 记录单据中每个商品的详细信息
 */
@Getter
@Setter
@TableName("stock_document_item")
public class StockDocumentItem extends BaseEntity {
    
    /**
     * 单据ID
     */
    @TableField("document_id")
    private String documentId;
    
    /**
     * 商品SKU ID
     */
    @TableField("sku_id")
    private String skuId;
    
    /**
     * 数量
     */
    private Integer quantity;
    
    /**
     * 单价
     */
    @TableField("unit_price")
    private BigDecimal unitPrice;
    
    /**
     * 小计金额
     */
    @TableField("subtotal_amount")
    private BigDecimal subtotalAmount;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建单据明细
     */
    public static StockDocumentItem create(String skuId, Integer quantity, BigDecimal unitPrice, String remark) {
        if (skuId == null || skuId.trim().isEmpty()) {
            throw new IllegalArgumentException("SKU ID不能为空");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("数量必须大于0");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("单价不能为负数");
        }
        
        StockDocumentItem item = new StockDocumentItem();
        item.skuId = skuId;
        item.quantity = quantity;
        item.unitPrice = unitPrice;
        item.subtotalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        item.remark = remark;
        
        return item;
    }
    
    /**
     * 更新数量
     */
    public void updateQuantity(Integer newQuantity) {
        if (newQuantity == null || newQuantity <= 0) {
            throw new IllegalArgumentException("数量必须大于0");
        }
        
        this.quantity = newQuantity;
        this.subtotalAmount = this.unitPrice.multiply(BigDecimal.valueOf(newQuantity));
    }
    
    /**
     * 更新单价
     */
    public void updateUnitPrice(BigDecimal newUnitPrice) {
        if (newUnitPrice == null || newUnitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("单价不能为负数");
        }
        
        this.unitPrice = newUnitPrice;
        this.subtotalAmount = newUnitPrice.multiply(BigDecimal.valueOf(this.quantity));
    }
    
    /**
     * 更新数量和单价
     */
    public void updateQuantityAndPrice(Integer newQuantity, BigDecimal newUnitPrice) {
        if (newQuantity == null || newQuantity <= 0) {
            throw new IllegalArgumentException("数量必须大于0");
        }
        if (newUnitPrice == null || newUnitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("单价不能为负数");
        }
        
        this.quantity = newQuantity;
        this.unitPrice = newUnitPrice;
        this.subtotalAmount = newUnitPrice.multiply(BigDecimal.valueOf(newQuantity));
    }
    
    /**
     * 获取小计金额
     */
    public BigDecimal getSubtotalAmount() {
        if (subtotalAmount == null) {
            subtotalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
        return subtotalAmount;
    }
}