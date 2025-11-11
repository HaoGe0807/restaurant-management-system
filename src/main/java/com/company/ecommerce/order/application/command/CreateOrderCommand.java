package com.company.ecommerce.order.application.command;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建订单命令
 */
@Data
public class CreateOrderCommand {
    
    private Long userId;
    
    private List<OrderItemCommand> items;
    
    @Data
    public static class OrderItemCommand {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
    }
}

