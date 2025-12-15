package com.restaurant.management.product.domain.event;

import com.restaurant.management.common.domain.DomainEvent;
import lombok.Getter;

import java.util.List;

/**
 * 商品删除事件
 * 当商品被删除时发布此事件，用于清理相关的库存数据
 */
@Getter
public class ProductDeletionEvent extends DomainEvent {
    
    private final String spuId;
    private final List<String> skuIds;
    
    public ProductDeletionEvent(String spuId, List<String> skuIds) {
        super();
        this.spuId = spuId;
        this.skuIds = skuIds;
    }
}