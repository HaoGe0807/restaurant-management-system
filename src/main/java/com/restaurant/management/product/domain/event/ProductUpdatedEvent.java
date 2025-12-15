package com.restaurant.management.product.domain.event;

import com.restaurant.management.common.domain.DomainEvent;
import com.restaurant.management.product.domain.model.ProductSku;
import lombok.Getter;

import java.util.List;

/**
 * 商品更新事件
 * 当商品信息发生变更时发布此事件，用于同步更新库存中的商品信息
 */
@Getter
public class ProductUpdatedEvent extends DomainEvent {
    
    private final String spuId;
    private final String oldSpuName;
    private final String newSpuName;
    private final List<ProductSku> skus;
    
    public ProductUpdatedEvent(String spuId, String oldSpuName, String newSpuName, List<ProductSku> skus) {
        super();
        this.spuId = spuId;
        this.oldSpuName = oldSpuName;
        this.newSpuName = newSpuName;
        this.skus = skus;
    }
}