package com.restaurant.management.inventory.application.command;

import lombok.Data;

/**
 * 预留库存命令
 */
@Data
public class ReserveInventoryCommand {
    
    private String productId;
    private Integer quantity;
}

