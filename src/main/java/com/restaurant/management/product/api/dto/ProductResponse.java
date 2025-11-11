package com.restaurant.management.product.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品响应DTO
 */
@Data
public class ProductResponse {
    
    private Long id;
    private String productName;
    private String description;
    private BigDecimal price;
    private String status;
    private Long categoryId;
    private LocalDateTime createTime;
}

