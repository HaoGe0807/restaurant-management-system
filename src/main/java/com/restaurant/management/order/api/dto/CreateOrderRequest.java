package com.restaurant.management.order.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建订单请求DTO
 */
@Data
public class CreateOrderRequest {
    
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    @NotEmpty(message = "订单项不能为空")
    private List<OrderItemRequest> items;
    
    @Data
    public static class OrderItemRequest {
        @NotNull(message = "SKU ID不能为空")
        private String skuId;
        
        @NotNull(message = "SKU名称不能为空")
        private String skuName;
        
        @NotNull(message = "数量不能为空")
        private Integer quantity;
        
        @NotNull(message = "单价不能为空")
        private BigDecimal unitPrice;
    }
}

