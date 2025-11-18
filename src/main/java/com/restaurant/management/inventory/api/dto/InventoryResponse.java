package com.restaurant.management.inventory.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库存响应DTO
 */
@Data
public class InventoryResponse {
    
    private Long id;
    private String skuId;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private Integer totalQuantity;
    private LocalDateTime createTime;
}

