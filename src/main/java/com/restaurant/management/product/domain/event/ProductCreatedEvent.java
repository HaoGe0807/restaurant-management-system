package com.restaurant.management.product.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    /**
     * 构造一个新的ProductCreatedEvent对象。
     * @param spuId 产品的spuId。
     * @param spuName 产品的spu名称。
     * @param skus 产品的sku快照列表。
     */
    @JsonCreator
    public ProductCreatedEvent(@JsonProperty("spuId") String spuId,
                               @JsonProperty("spuName") String spuName,
                               @JsonProperty("skus") List<SkuSnapshot> skus) {
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

        @JsonCreator
        public SkuSnapshot(@JsonProperty("skuId") String skuId,
                           @JsonProperty("skuName") String skuName,
                           @JsonProperty("initialQuantity") int initialQuantity) {
            this.skuId = skuId;
            this.skuName = skuName;
            this.initialQuantity = initialQuantity;
        }
    }
}
