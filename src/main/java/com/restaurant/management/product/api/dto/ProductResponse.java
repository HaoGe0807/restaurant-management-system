package com.restaurant.management.product.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品响应DTO（SPU + SKU）
 */
@Data
public class ProductResponse {

    private String spuId;
    private String spuName;
    private String description;
    private String status;
    private LocalDateTime createTime;
    private List<SkuResponse> skus;

    @Data
    public static class SkuResponse {
        private String skuId;
        private String skuName;
        private BigDecimal price;
        private String attributes;
    }
}

