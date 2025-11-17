package com.restaurant.management.product.domain.event;

import com.restaurant.management.common.domain.DomainEvent;
import lombok.Getter;

/**
 * 商品已创建事件
 * 表示商品聚合中发生了一个重要的事实：商品已创建
 * 用于通知其他限界上下文（如库存上下文）
 */
@Getter
public class ProductCreatedEvent extends DomainEvent {
    
    private final Long productId;
    private final String productName;
    private final int initialQuantity;  // 初始库存数量
    
    public ProductCreatedEvent(Long productId, String productName, int initialQuantity) {
        super();
        this.productId = productId;
        this.productName = productName;
        this.initialQuantity = initialQuantity;
    }
}

