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
    
    private String productId;
    
    private String productName;
    
    private Integer quantity;
    
    private BigDecimal unitPrice;
    
    private BigDecimal subTotal;
    
    @TableField(exist = false)
    @JsonIgnore
    private Order order; // 保持领域模型引用，不参与持久化
    
    /**
     * 创建订单项
     */
    public static OrderItem create(String productId, String productName, 
                                   Integer quantity, BigDecimal unitPrice) {
        OrderItem item = new OrderItem();
        item.productId = productId;
        item.productName = productName;
        item.quantity = quantity;
        item.unitPrice = unitPrice;
        item.subTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        return item;
    }
}

