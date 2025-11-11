package com.restaurant.management.product.application.command;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建商品命令
 */
@Data
public class CreateProductCommand {
    
    private String productName;
    private String description;
    private BigDecimal price;
    private Long categoryId;
    
    /**
     * 初始库存数量（创建商品时同时创建库存）
     */
    private Integer initialQuantity;
}

