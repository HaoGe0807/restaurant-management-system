package com.restaurant.management.product.domain.event;

import com.restaurant.management.common.domain.DomainEvent;
import com.restaurant.management.product.domain.model.ProductStatus;
import lombok.Getter;

/**
 * 商品状态变更事件
 * 当商品状态发生变更时发布此事件，用于同步库存的冻结/解冻状态
 */
@Getter
public class ProductStatusChangedEvent extends DomainEvent {
    
    private final String spuId;
    private final ProductStatus oldStatus;
    private final ProductStatus newStatus;
    
    public ProductStatusChangedEvent(String spuId, ProductStatus oldStatus, ProductStatus newStatus) {
        super();
        this.spuId = spuId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }
}