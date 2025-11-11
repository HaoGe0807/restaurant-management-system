package com.company.ecommerce.order.domain.model;

import com.company.ecommerce.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 订单项实体
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
public class OrderItem extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "product_name", nullable = false)
    private String productName;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;
    
    @Column(name = "sub_total", nullable = false)
    private BigDecimal subTotal;
    
    /**
     * 创建订单项
     */
    public static OrderItem create(Long productId, String productName, 
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

