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
}

