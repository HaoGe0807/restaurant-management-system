package com.restaurant.management.inventory.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 预留库存请求DTO
 */
@Data
public class ReserveInventoryRequest {
    
    @NotNull(message = "SKU ID不能为空")
    private String skuId;
    
    @NotNull(message = "数量不能为空")
    @Positive(message = "数量必须大于0")
    private Integer quantity;
    
    @NotNull(message = "订单ID不能为空")
    private String orderId;
}

