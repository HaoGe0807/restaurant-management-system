package com.restaurant.management.product.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建商品请求DTO
 */
@Data
public class CreateProductRequest {
    
    @NotBlank(message = "商品名称不能为空")
    private String productName;
    
    private String description;
    
    @NotNull(message = "价格不能为空")
    @Positive(message = "价格必须大于0")
    private BigDecimal price;
    
    private Long categoryId;
}

