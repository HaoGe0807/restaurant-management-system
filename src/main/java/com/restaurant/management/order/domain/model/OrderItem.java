package com.restaurant.management.order.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.restaurant.management.common.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 订单项实体
 */
@Getter
@Setter
@TableName("order_items")
public class OrderItem extends BaseEntity {
    
    private Long orderId;
    
    @TableField("spu_id")
    private String spuId;
    
    @TableField("sku_id")
    private String skuId;
    
    @TableField("sku_name")
    private String skuName;
    
    private Integer quantity;
    
    private BigDecimal unitPrice;
    
    private BigDecimal subTotal;
    
    @TableField(exist = false)
    @JsonIgnore
    private Order order; // 保持领域模型引用，不参与持久化
    
    /**
     * 创建订单项
     */
    public static OrderItem create(String spuId, String skuId, String skuName,
                                   Integer quantity, BigDecimal unitPrice) {
        OrderItem item = new OrderItem();
        item.spuId = spuId;
        item.skuId = skuId;
        item.skuName = skuName;
        item.quantity = quantity;
        item.unitPrice = unitPrice;
        item.subTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        return item;
    }
}

