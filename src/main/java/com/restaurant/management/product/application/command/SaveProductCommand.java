package com.restaurant.management.product.application.command;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建商品命令（包含 SPU 与 SKU 信息）
 */
@Data
public class SaveProductCommand {

    private String spuName;
    private String description;
    private List<SkuCommand> skus;

    @Data
    public static class SkuCommand {
        private String skuName;
        private BigDecimal price;
        private String attributes;
        private int initialQuantity;
    }
}
