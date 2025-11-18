package com.restaurant.management.product.domain.event;

import com.restaurant.management.common.domain.DomainEvent;
import lombok.Getter;

import java.util.List;

/**
 * 商品创建事件（SPU + SKU）
 */
@Getter
public class ProductCreatedEvent extends DomainEvent {

    private final String spuId;
    private final String spuName;
    private final List<SkuSnapshot> skus;

    public ProductCreatedEvent(String spuId, String spuName, List<SkuSnapshot> skus) {
        super();
        this.spuId = spuId;
        this.spuName = spuName;
        this.skus = skus;
    }

    @Getter
    public static class SkuSnapshot {
        private final String skuId;
        private final String skuName;
        private final int initialQuantity;

        public SkuSnapshot(String skuId, String skuName, int initialQuantity) {
            this.skuId = skuId;
            this.skuName = skuName;
            this.initialQuantity = initialQuantity;
        }
    }
}
